package com.gamerzone.carrito.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Solicitud provisional para actualizar la cantidad de un elemento.")
public record ActualizarCantidadRequest(
        @Schema(description = "Nueva cantidad solicitada.", example = "3")
        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor que cero.")
        Integer cantidad) {
}
