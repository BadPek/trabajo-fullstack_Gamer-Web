package com.gamerzone.pedido.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.gamerzone.pedido.dto.integration.CarritoResponse;
import com.gamerzone.pedido.dto.integration.ProductoResponse;
import com.gamerzone.pedido.dto.request.CrearPedidoRequest;
import com.gamerzone.pedido.dto.response.ItemPedidoResponse;
import com.gamerzone.pedido.dto.response.PedidoResponse;
import com.gamerzone.pedido.entity.Pedido;
import com.gamerzone.pedido.entity.PedidoItem;
import com.gamerzone.pedido.exception.ApiException;
import com.gamerzone.pedido.integration.CarritoClient;
import com.gamerzone.pedido.integration.CatalogoClient;
import com.gamerzone.pedido.repository.PedidoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoWriter pedidoWriter;
    private final CarritoClient carritoClient;
    private final CatalogoClient catalogoClient;

    public PedidoService(PedidoRepository pedidoRepository, PedidoWriter pedidoWriter,
            CarritoClient carritoClient, CatalogoClient catalogoClient) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoWriter = pedidoWriter;
        this.carritoClient = carritoClient;
        this.catalogoClient = catalogoClient;
    }

    public PedidoResponse create(String customerId, String bearerToken, String idempotencyKey,
            CrearPedidoRequest request) {
        String address = request.direccionDespacho().trim();
        String requestHash = PedidoFingerprint.sha256(address);
        var existing = pedidoRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey);
        if (existing.isPresent()) {
            Pedido saved = existing.get();
            if (!saved.getRequestHash().equals(requestHash)) {
                throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
                        "La clave de idempotencia ya se usó con otros datos de entrega.");
            }
            return toResponse(saved);
        }

        CarritoResponse cart = carritoClient.getCurrentCart(bearerToken);
        if (cart.items().isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "EMPTY_CART",
                    "No se puede crear un pedido con el carrito vacío.");
        }

        Map<String, ProductoResponse> products = new LinkedHashMap<>();
        long total = 0;
        for (CarritoResponse.ItemCarrito cartItem : cart.items()) {
            if (cartItem == null || cartItem.productoId() == null || cartItem.productoId().isBlank()
                    || cartItem.cantidad() == null || cartItem.cantidad() < 1) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "INVALID_CART_ITEM",
                        "ms-carrito devolvió un elemento inválido.");
            }
            ProductoResponse product = products.computeIfAbsent(
                    cartItem.productoId(), catalogoClient::findProduct);
            if (product == null || product.precio() == null || product.titulo() == null
                    || product.titulo().isBlank() || product.precio() < 0) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "INVALID_PRODUCT_PRICE",
                        "ms-catalogo devolvió datos de producto inválidos.");
            }
            try {
                long lineTotal = Math.multiplyExact(product.precio().longValue(), cartItem.cantidad());
                total = Math.addExact(total, lineTotal);
            } catch (ArithmeticException exception) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "ORDER_TOTAL_OVERFLOW",
                        "El total del pedido excede el valor permitido.");
            }
        }

        Pedido pedido = new Pedido(customerId, idempotencyKey, requestHash, address, total);
        for (CarritoResponse.ItemCarrito cartItem : cart.items()) {
            ProductoResponse product = products.get(cartItem.productoId());
            long lineTotal = Math.multiplyExact(product.precio().longValue(), cartItem.cantidad());
            pedido.addItem(new PedidoItem(cartItem.productoId(), product.titulo(),
                    cartItem.cantidad(), product.precio(), lineTotal));
        }
        try {
            return toResponse(pedidoWriter.save(pedido));
        } catch (DataIntegrityViolationException exception) {
            var concurrentRequest = pedidoRepository.findByCustomerIdAndIdempotencyKey(customerId, idempotencyKey);
            if (concurrentRequest.isEmpty()) {
                throw exception;
            }
            Pedido saved = concurrentRequest.get();
            if (!saved.getRequestHash().equals(requestHash)) {
                throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSED",
                        "La clave de idempotencia ya se usó con otros datos de entrega.");
            }
            return toResponse(saved);
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

    private static PedidoResponse toResponse(Pedido pedido) {
        List<ItemPedidoResponse> items = pedido.getItems().stream()
                .map(item -> new ItemPedidoResponse(item.getProductId(), item.getProductName(),
                        item.getQuantity(), item.getUnitPrice(), item.getLineTotal()))
                .toList();
        return new PedidoResponse(pedido.getId(), pedido.getStatus(), pedido.getShippingAddress(),
                pedido.getCurrency(), pedido.getTotalAmount(), pedido.getCreatedAt(), items);
    }
}
