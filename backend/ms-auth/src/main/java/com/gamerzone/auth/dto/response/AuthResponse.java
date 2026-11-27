package com.gamerzone.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT de acceso y perfil de cuenta autenticado.")
public record AuthResponse(
        String tokenType,
        String accessToken,
        long expiresIn,
        UsuarioResponse user) {
}
