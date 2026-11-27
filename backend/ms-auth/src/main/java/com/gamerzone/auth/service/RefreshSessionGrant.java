package com.gamerzone.auth.service;

import java.time.Instant;

import com.gamerzone.auth.entity.Usuario;

public record RefreshSessionGrant(Usuario usuario, String refreshToken, Instant expiresAt) {
}
