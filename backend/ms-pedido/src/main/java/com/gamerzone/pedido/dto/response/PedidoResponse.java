package com.gamerzone.pedido.dto.response;

import java.time.Instant;
import java.util.List;

/** Pedido con sus snapshots. El total se calcula desde las líneas; no se guarda dirección. */
public record PedidoResponse(
        String id,
        String estado,
        String moneda,
        long total,
        Instant creadoEn,
        List<ItemPedidoResponse> items) {
}
