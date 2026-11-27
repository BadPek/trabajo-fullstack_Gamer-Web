package com.gamerzone.auth.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Perfil administrativo sin contraseñas ni secretos.")
public record AdminUsuarioResponse(
        String id,
        String rut,
        String nombre,
        String apellidos,
        String correo,
        String region,
        String comuna,
        String direccion,
        String rol,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime anonymizedAt) {
}
