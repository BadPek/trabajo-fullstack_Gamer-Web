package com.gamerzone.carrito.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Max;

@Schema(description = "Solicitud provisional para agregar un producto. No acepta precios.")
public record AgregarProductoRequest(
        @Schema(description = "Identificador UUID del producto en ms-catalogo.",
                example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "El identificador del producto es obligatorio.")
        @Pattern(regexp = "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
                message = "El identificador del producto debe ser un UUID.")
        String productoId,

        @Schema(description = "Cantidad solicitada.", example = "2")
        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor que cero.")
        @Max(value = 99, message = "La cantidad máxima por producto es 99.")
        Integer cantidad) {
}
