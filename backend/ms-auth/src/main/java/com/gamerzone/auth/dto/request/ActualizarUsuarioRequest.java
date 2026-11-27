package com.gamerzone.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Campos editables del perfil administrativo; no permite cambiar rol ni estado.")
public record ActualizarUsuarioRequest(
        @Size(max = 12) String rut,
        @Pattern(regexp = ".*\\S.*") @Size(max = 50) String nombre,
        @Pattern(regexp = ".*\\S.*") @Size(max = 100) String apellidos,
        @Email @Size(max = 100) String correo,
        @Pattern(regexp = ".*\\S.*") @Size(max = 50) String region,
        @Pattern(regexp = ".*\\S.*") @Size(max = 100) String comuna,
        @Pattern(regexp = ".*\\S.*") @Size(max = 300) String direccion) {
}
