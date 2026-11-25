package com.gamerzone.carrito.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Representación provisional del contenido del carrito.")
public record CarritoResponse(
        @Schema(description = "Elementos del carrito.")
        List<ItemCarritoResponse> items,
        @Schema(description = "Subtotal en pesos chilenos.")
        Long subtotal,
        @Schema(description = "Descuento aplicado en pesos chilenos.")
        Long descuento,
        @Schema(description = "Total en pesos chilenos.")
        Long total) {
}
