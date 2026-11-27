package com.gamerzone.carrito.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Carrito autenticado con versión, cupón, artículos y totales.")
public record CarritoResponse(
        long version,
        @Schema(description = "Elementos del carrito.")
        List<ItemCarritoResponse> items,
        @Schema(description = "Cupón aplicado, o null si no hay cupón.")
        CartCouponResponse coupon,
        @Schema(description = "Subtotal en pesos chilenos.")
        Long subtotal,
        @Schema(description = "Descuento aplicado en pesos chilenos.")
        Long discountAmount,
        @Schema(description = "IVA incluido informativo en pesos chilenos.")
        Long includedVat,
        @Schema(description = "Total en pesos chilenos.")
        Long total) {
}
