package com.gamerzone.pedido.service;

import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.gamerzone.pedido.dto.integration.CarritoResponse;
import com.gamerzone.pedido.dto.integration.ProductoResponse;
import com.gamerzone.pedido.dto.request.CrearPedidoRequest;
import com.gamerzone.pedido.dto.response.ItemPedidoResponse;
import com.gamerzone.pedido.dto.response.PedidoResponse;
import com.gamerzone.pedido.entity.Pedido;
import com.gamerzone.pedido.entity.PedidoIdempotencia;
import com.gamerzone.pedido.entity.PedidoIdempotenciaId;
import com.gamerzone.pedido.entity.PedidoItem;
import com.gamerzone.pedido.exception.ApiException;
import com.gamerzone.pedido.integration.CarritoClient;
import com.gamerzone.pedido.integration.CatalogoClient;
import com.gamerzone.pedido.repository.PedidoIdempotenciaRepository;
import com.gamerzone.pedido.repository.PedidoRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
            return replay(existing.get(), customerId, cartVersion, fingerprint);
        }

        CarritoResponse cart = carritoClient.getCurrentCart(bearerToken);
        if (cart.items().isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "EMPTY_CART",
                    "No se puede crear un pedido con el carrito vacío.");
        }
        Map<String, ProductoResponse> products = new LinkedHashMap<>();
        for (CarritoResponse.ItemCarrito cartItem : cart.items()) {
            if (cartItem == null || cartItem.productoId() == null || cartItem.productoId().isBlank()
                    || cartItem.cantidad() == null || cartItem.cantidad() < 1
                    || cartItem.cantidad() > CANTIDAD_MAXIMA) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "INVALID_CART_ITEM",
                        "ms-carrito devolvió un elemento inválido.");
            }
            ProductoResponse product = products.computeIfAbsent(
                    cartItem.productoId(), catalogoClient::findProduct);
            if (product == null || product.id() == null || product.precio() == null || product.precio() <= 0
                    || product.titulo() == null || product.titulo().isBlank()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "INVALID_PRODUCT_PRICE",
                        "ms-catalogo devolvió datos de producto inválidos.");
            }
        }

        Pedido pedido = new Pedido(customerId);
        for (CarritoResponse.ItemCarrito cartItem : cart.items()) {
            ProductoResponse product = products.get(cartItem.productoId());
            pedido.addItem(new PedidoItem(product.id(), product.titulo().trim(),
                    cartItem.cantidad(), product.precio()));
        }
        try {
            return toResponse(pedidoWriter.save(pedido, key, cartVersion, fingerprint));
        } catch (DataIntegrityViolationException exception) {
            var concurrentRequest = idempotenciaRepository.findById(idempotenciaId);
            if (concurrentRequest.isEmpty()) {
                throw exception;
            }
            return replay(concurrentRequest.get(), customerId, cartVersion, fingerprint);
        }
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> findMine(String customerId) {
        return pedidoRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(PedidoService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse findMineById(String id, String customerId) {
        return pedidoRepository.findByIdAndCustomerId(id, customerId)
                .map(PedidoService::toResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND",
                        "No se encontró el pedido solicitado."));
    }

    /** Reintento con una clave ya usada: misma versión y cotización devuelven el pedido original. */
    private PedidoResponse replay(PedidoIdempotencia registro, String customerId, long cartVersion,
            String fingerprint) {
        if (!registro.matches(cartVersion, fingerprint)) {
            throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
                    "La clave de idempotencia ya se usó con otra versión del carrito o cotización.");
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
}
