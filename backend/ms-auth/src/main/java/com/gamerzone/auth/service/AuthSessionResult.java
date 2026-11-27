package com.gamerzone.auth.service;

import com.gamerzone.auth.dto.response.AuthResponse;

public record AuthSessionResult(AuthResponse response, String refreshToken, long refreshMaxAgeSeconds) {
}
