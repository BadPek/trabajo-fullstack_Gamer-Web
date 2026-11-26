package com.gamerzone.carrito.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Artículo de producto en el carrito.")
public record ItemCarritoResponse(
        @Schema(description = "Identificador UUID del producto.")
        String productId,
        @Schema(description = "Título del producto.")
        String titulo,
        @Schema(description = "Imagen de portada del producto.")
        String imagen,
        @Schema(description = "Precio unitario en pesos chilenos.")
        Long precioUnitario,
        @Schema(description = "Cantidad solicitada.")
        Integer cantidad,
        @Schema(description = "Subtotal de la línea en pesos chilenos.")
        Long subtotal) {
}
