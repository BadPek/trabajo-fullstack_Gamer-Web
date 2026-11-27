package com.gamerzone.auth.service;

import java.time.Instant;

public record RefreshTokenGrant(String refreshToken, Instant expiresAt) {
}
