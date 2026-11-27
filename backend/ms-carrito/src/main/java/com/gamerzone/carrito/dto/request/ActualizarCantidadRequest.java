package com.gamerzone.carrito.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;

@Schema(description = "Solicitud para establecer la cantidad final de una línea del carrito.")
public record ActualizarCantidadRequest(
        @Schema(description = "Nueva cantidad solicitada.", example = "3")
        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor que cero.")
        @Max(value = 99, message = "La cantidad máxima por producto es 99.")
        Integer quantity) {
}
