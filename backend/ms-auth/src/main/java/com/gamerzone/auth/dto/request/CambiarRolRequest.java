package com.gamerzone.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Rol asignado a una cuenta.")
public record CambiarRolRequest(
        @NotBlank @Pattern(regexp = "CLIENTE|ADMINISTRADOR") String rol) {
}
