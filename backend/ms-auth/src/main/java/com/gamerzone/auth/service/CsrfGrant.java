package com.gamerzone.auth.service;

import java.time.Instant;

public record CsrfGrant(String contextCookie, String token, Instant tokenExpiresAt, long contextMaxAgeSeconds) {
}
