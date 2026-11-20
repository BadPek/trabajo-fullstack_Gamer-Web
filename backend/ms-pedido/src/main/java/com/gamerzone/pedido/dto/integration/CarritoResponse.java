package com.gamerzone.pedido.dto.integration;

import java.util.List;

public record CarritoResponse(List<ItemCarrito> items) {

    public record ItemCarrito(String id, String productoId, Integer cantidad) {
    }
}
