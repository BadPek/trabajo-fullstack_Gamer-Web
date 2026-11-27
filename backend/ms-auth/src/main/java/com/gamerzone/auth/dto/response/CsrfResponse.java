package com.gamerzone.auth.dto.response;

import java.time.Instant;

public record CsrfResponse(String token, Instant expiresAt) {
}
