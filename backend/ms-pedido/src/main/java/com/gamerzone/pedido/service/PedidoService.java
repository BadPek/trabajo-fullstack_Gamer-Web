package com.gamerzone.pedido.service;

import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.List;

import com.gamerzone.pedido.dto.request.CrearPedidoRequest;
import com.gamerzone.pedido.dto.response.ItemPedidoResponse;
import com.gamerzone.pedido.dto.response.PageResponse;
import com.gamerzone.pedido.dto.response.PedidoResponse;
import com.gamerzone.pedido.entity.Pedido;
import com.gamerzone.pedido.entity.PedidoIdempotencia;
import com.gamerzone.pedido.entity.PedidoIdempotenciaId;
import com.gamerzone.pedido.entity.PedidoItem;
import com.gamerzone.pedido.entity.PedidoCouponSnapshot;
import com.gamerzone.pedido.dto.integration.QuoteValidationResponse;
import com.gamerzone.pedido.exception.ApiException;
import com.gamerzone.pedido.exception.ExternalDependencyException;
import com.gamerzone.pedido.integration.CarritoClient;
import com.gamerzone.pedido.integration.CatalogoClient;
import com.gamerzone.pedido.repository.PedidoIdempotenciaRepository;
import com.gamerzone.pedido.repository.PedidoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private static final String MONEDA = "CLP";
    private static final int CANTIDAD_MAXIMA = 99;

    private final PedidoRepository pedidoRepository;
    private final PedidoIdempotenciaRepository idempotenciaRepository;
    private final PedidoWriter pedidoWriter;
    private final CarritoClient carritoClient;
    private final CatalogoClient catalogoClient;

    public PedidoService(PedidoRepository pedidoRepository, PedidoIdempotenciaRepository idempotenciaRepository,
            PedidoWriter pedidoWriter, CarritoClient carritoClient, CatalogoClient catalogoClient) {
        this.pedidoRepository = pedidoRepository;
        this.idempotenciaRepository = idempotenciaRepository;
        this.pedidoWriter = pedidoWriter;
        this.carritoClient = carritoClient;
        this.catalogoClient = catalogoClient;
    }

    public PedidoResponse create(String customerId, String bearerToken, String idempotencyKey,
            CrearPedidoRequest request) {
        String key = idempotencyKey.trim().toLowerCase(Locale.ROOT);
        long cartVersion = request.cartVersion();
        String fingerprint = PedidoFingerprint.sha256(request.priceQuoteToken().trim());
        PedidoIdempotenciaId idempotenciaId = new PedidoIdempotenciaId(customerId, key);

        var existing = idempotenciaRepository.findById(idempotenciaId);
        if (existing.isPresent()) {
            return replay(existing.get(), customerId, cartVersion, fingerprint, bearerToken);
        }

        QuoteValidationResponse quote = carritoClient.validateQuote(
                bearerToken, cartVersion, request.priceQuoteToken().trim());
        if (quote == null || quote.items() == null || quote.items().isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "EMPTY_CART",
                    "No se puede crear un pedido con el carrito vacío.");
        }
        for (QuoteValidationResponse.QuoteItemSnapshot cartItem : quote.items()) {
            if (cartItem == null || cartItem.productId() == null || cartItem.productId().isBlank()
                    || cartItem.quantity() < 1 || cartItem.quantity() > CANTIDAD_MAXIMA
                    || cartItem.titleSnapshot() == null || cartItem.titleSnapshot().isBlank()
                    || cartItem.unitPriceClp() <= 0) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "INVALID_CART_ITEM",
                        "ms-carrito devolvió un elemento inválido.");
            }
        }

        Pedido pedido = new Pedido(customerId);
        for (QuoteValidationResponse.QuoteItemSnapshot cartItem : quote.items()) {
            pedido.addItem(new PedidoItem(
                    cartItem.productId(),
                    cartItem.titleSnapshot().trim(),
                    cartItem.quantity(),
                    cartItem.unitPriceClp()));
        }
        if (quote.couponSnapshot() != null) {
            pedido.setCouponSnapshot(new PedidoCouponSnapshot(
                    pedido,
                    quote.couponSnapshot().code(),
                    quote.couponSnapshot().discountPercent()));
        }

        var reservation = carritoClient.createReservation(
                bearerToken, cartVersion, idempotencyKey);
        String reservationId = reservation.reservationId();
        Pedido saved;
        try {
            saved = pedidoWriter.save(
                    pedido, key, cartVersion, fingerprint, reservationId);
        } catch (DataIntegrityViolationException exception) {
            var concurrentRequest = idempotenciaRepository.findById(idempotenciaId);
            if (concurrentRequest.isEmpty()) {
                try {
                    carritoClient.releaseReservation(bearerToken, reservationId);
                } catch (RuntimeException releaseException) {
                    exception.addSuppressed(releaseException);
                }
                throw exception;
            }
            return replay(concurrentRequest.get(), customerId, cartVersion, fingerprint, bearerToken);
        } catch (ApiException | ExternalDependencyException exception) {
            try {
                carritoClient.releaseReservation(bearerToken, reservationId);
            } catch (RuntimeException releaseException) {
                exception.addSuppressed(releaseException);
            }
            throw exception;
        } catch (RuntimeException exception) {
            try {
                carritoClient.releaseReservation(bearerToken, reservationId);
            } catch (RuntimeException releaseException) {
                exception.addSuppressed(releaseException);
            }
            throw exception;
        }
        carritoClient.consumeQuote(bearerToken, cartVersion, request.priceQuoteToken().trim());
        carritoClient.completeReservation(bearerToken, reservationId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<PedidoResponse> findMine(String customerId, int page, int size) {
        var pageable = PageRequest.of(page, size, orderSort());
        return PageResponse.from(pedidoRepository.findByCustomerId(customerId, pageable)
                .map(PedidoService::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<PedidoResponse> findAll(
            int page, int size, String status, OffsetDateTime from, OffsetDateTime to) {
        Specification<Pedido> specification = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }
        if (from != null) {
            var fromUtc = from.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.<LocalDateTime>get("createdAt"), fromUtc));
        }
        if (to != null) {
            var toUtc = to.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.<LocalDateTime>get("createdAt"), toUtc));
        }
        var pageable = PageRequest.of(page, size, orderSort());
        return PageResponse.from(pedidoRepository.findAll(specification, pageable)
                .map(PedidoService::toResponse));
    }

    @Transactional(readOnly = true)
    public PedidoResponse findById(String id) {
        return pedidoRepository.findById(id)
                .map(PedidoService::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND",
                        "No se encontró el pedido solicitado."));
    }

    /** Reintento con una clave ya usada: misma versión y cotización devuelven el pedido original. */
    private PedidoResponse replay(PedidoIdempotencia registro, String customerId, long cartVersion,
            String fingerprint, String bearerToken) {
        if (!registro.matches(cartVersion, fingerprint)) {
            throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
                    "La clave de idempotencia ya se usó con otra versión del carrito o cotización.");
        }
        if (registro.getReservationId() != null && !registro.getReservationId().isBlank()) {
            carritoClient.completeReservation(bearerToken, registro.getReservationId());
        }
        return pedidoRepository.findByIdAndCustomerId(registro.getOrderId(), customerId)
                .map(PedidoService::toResponse)
                .orElseThrow(() -> new IllegalStateException(
                        "La clave de idempotencia apunta a un pedido que no existe."));
    }

    private static PedidoResponse toResponse(Pedido pedido) {
        List<ItemPedidoResponse> items = pedido.getItems().stream()
                .map(item -> new ItemPedidoResponse(item.getProductId(), item.getProductName(),
                        item.getQuantity(), item.getUnitPrice(), item.getLineTotal()))
                .toList();
        return new PedidoResponse(pedido.getId(), pedido.getStatus(), MONEDA, pedido.getTotal(),
                pedido.getCreatedAt() == null ? null : pedido.getCreatedAt().toInstant(ZoneOffset.UTC), items);
    }

    private static Sort orderSort() {
        return Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"));
    }
}
