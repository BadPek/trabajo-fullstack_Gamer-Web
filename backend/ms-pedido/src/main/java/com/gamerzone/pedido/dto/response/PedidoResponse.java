package com.gamerzone.pedido.dto.response;

import java.time.Instant;
import java.util.List;

public record PedidoResponse(
        String id,
        String estado,
        String direccionDespacho,
        String moneda,
        long total,
        Instant creadoEn,
        List<ItemPedidoResponse> items) {
}
