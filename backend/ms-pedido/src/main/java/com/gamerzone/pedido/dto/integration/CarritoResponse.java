package com.gamerzone.pedido.dto.integration;

import java.util.List;

public record CarritoResponse(List<ItemCarrito> items) {

    public record ItemCarrito(
            String productId,
            String titulo,
            String imagen,
            Integer precioUnitario,
            Integer cantidad,
            Integer subtotal) {
    }
}
