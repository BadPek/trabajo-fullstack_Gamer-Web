package com.gamerzone.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar una cuenta GamerZone.")
public record RegistroRequest(
        @NotBlank @Size(max = 12) String rut,
        @NotBlank @Size(max = 50) String nombre,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Email @Size(max = 100) String correo,
        @NotBlank @Size(min = 12, max = 128) String password,
        @NotBlank @Size(max = 50) String region,
        @NotBlank @Size(max = 100) String comuna,
        @NotBlank @Size(max = 300) String direccion) {
}
