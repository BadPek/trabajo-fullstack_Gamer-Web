package com.gamerzone.carrito.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Solicitud para reservar temporalmente el carrito antes de crear un pedido.")
public record CrearReservaRequest(
        @Schema(description = "Versión actual del carrito.")
        @NotNull @PositiveOrZero Long cartVersion,
        @Schema(description = "Clave UUID v4 de idempotencia.")
        @NotBlank
        @Pattern(regexp = "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
        String idempotencyKey) {
}
