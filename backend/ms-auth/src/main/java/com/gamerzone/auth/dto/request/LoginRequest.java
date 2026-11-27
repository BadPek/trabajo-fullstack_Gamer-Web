package com.gamerzone.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciales de acceso.")
public record LoginRequest(
        @NotBlank @Email @Size(max = 100) String correo,
        @NotBlank @Size(min = 12, max = 128) String password) {
}
