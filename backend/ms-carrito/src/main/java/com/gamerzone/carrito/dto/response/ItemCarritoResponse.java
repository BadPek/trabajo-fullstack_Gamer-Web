package com.gamerzone.carrito.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representación provisional de un elemento del carrito.")
public record ItemCarritoResponse(
        @Schema(description = "Identificador opaco del elemento.")
        String id,
        @Schema(description = "Identificador opaco del producto.")
        String productoId,
        @Schema(description = "Cantidad solicitada.")
        Integer cantidad) {
}
