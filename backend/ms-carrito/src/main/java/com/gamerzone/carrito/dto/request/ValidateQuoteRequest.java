package com.gamerzone.carrito.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos necesarios para validar una cotización interna.")
public record ValidateQuoteRequest(
        @NotBlank @Size(max = 4096) String priceQuoteToken,
        @NotNull @PositiveOrZero Long cartVersion) {
}
