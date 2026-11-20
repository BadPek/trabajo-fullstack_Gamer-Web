package com.gamerzone.pedido.dto.response;

public record ItemPedidoResponse(
        String productoId,
        String nombreProducto,
        int cantidad,
        long precioUnitario,
        long subtotal) {
}
