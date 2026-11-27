package com.gamerzone.carrito.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado del cálculo del cupón aplicado al carrito.")
public record CouponCalculationResponse(
        @Schema(description = "Código del cupón aplicado.")
        String code,
        @Schema(description = "Porcentaje de descuento.")
        int discountPercent,
        @Schema(description = "Subtotal de los productos.")
        long subtotal,
        @Schema(description = "Descuento redondeado al peso chileno.")
        long discount,
        @Schema(description = "Total después del descuento.")
        long total) {
}
