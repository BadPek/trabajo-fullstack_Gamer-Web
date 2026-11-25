package com.gamerzone.carrito.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representación provisional de un elemento del carrito.")
public record ItemCarritoResponse(
        @Schema(description = "Identificador derivado del elemento.")
        String id,
        @Schema(description = "Identificador UUID del producto.")
        String productoId,
        @Schema(description = "Título del producto.")
        String titulo,
        @Schema(description = "Precio unitario en pesos chilenos.")
        Long precioUnitario,
        @Schema(description = "Cantidad solicitada.")
        Integer cantidad,
        @Schema(description = "Subtotal de la línea en pesos chilenos.")
        Long subtotal) {
}
