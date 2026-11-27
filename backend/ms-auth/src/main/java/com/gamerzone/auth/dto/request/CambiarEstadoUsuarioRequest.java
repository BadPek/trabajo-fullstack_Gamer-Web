package com.gamerzone.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Estado asignado a una cuenta.")
public record CambiarEstadoUsuarioRequest(
        @NotBlank @Pattern(regexp = "ACTIVE|INACTIVE") String status) {
}
