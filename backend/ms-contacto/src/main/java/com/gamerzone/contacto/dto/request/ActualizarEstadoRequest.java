package com.gamerzone.contacto.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Nuevo estado administrativo del mensaje.")
public record ActualizarEstadoRequest(
        @Schema(description = "Estado permitido.", example = "LEIDO")
        @NotBlank(message = "El estado es obligatorio.")
        @Pattern(regexp = "RECIBIDO|LEIDO|CERRADO",
                message = "El estado debe ser RECIBIDO, LEIDO o CERRADO.")
        String estado) {
}
