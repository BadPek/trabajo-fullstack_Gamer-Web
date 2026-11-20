package com.gamerzone.contacto.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de validación asociado a un campo.")
public record ApiFieldError(
        @Schema(description = "Nombre del campo.")
        String field,
        @Schema(description = "Código de validación.")
        String code,
        @Schema(description = "Mensaje de validación.")
        String message) {
}
