package com.gamerzone.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Perfil público; nunca contiene la contraseña.")
public record UsuarioResponse(
        String id,
        String rut,
        String nombre,
        String apellido,
        String correo,
        String region,
        String comuna,
        String direccion) {
}
