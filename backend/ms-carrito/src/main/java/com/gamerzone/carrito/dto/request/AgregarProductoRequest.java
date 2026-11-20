package com.gamerzone.carrito.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Solicitud provisional para agregar un producto. No acepta precios.")
public record AgregarProductoRequest(
        @Schema(description = "Identificador opaco del producto en ms-catalogo.", example = "producto-123")
        @NotBlank(message = "El identificador del producto es obligatorio.")
        String productoId,

        @Schema(description = "Cantidad solicitada.", example = "2")
        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor que cero.")
        Integer cantidad) {
}
