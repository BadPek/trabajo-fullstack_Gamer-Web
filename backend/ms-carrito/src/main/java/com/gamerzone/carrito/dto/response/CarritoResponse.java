package com.gamerzone.carrito.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Representación provisional del contenido del carrito.")
public record CarritoResponse(
        @Schema(description = "Elementos del carrito.")
        List<ItemCarritoResponse> items) {
}
