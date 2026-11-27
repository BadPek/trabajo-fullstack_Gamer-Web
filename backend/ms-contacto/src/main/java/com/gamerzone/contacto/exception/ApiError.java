package com.gamerzone.contacto.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Respuesta de error con formato application/problem+json.")
public record ApiError(
        @Schema(description = "URI del tipo de problema.", example = "about:blank")
        String type,
        @Schema(description = "Título breve del problema.")
        String title,
        @Schema(description = "Código HTTP.")
        int status,
        @Schema(description = "Descripción segura del problema.")
        String detail,
        @Schema(description = "Ruta de la solicitud.")
        String instance,
        @Schema(description = "Código de error estable.")
        String code,
        @Schema(description = "Errores asociados a campos; vacío cuando no aplica.")
        List<ApiFieldError> fieldErrors) {
}
