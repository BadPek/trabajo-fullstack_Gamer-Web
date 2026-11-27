package com.gamerzone.pedido.dto.integration;

import java.time.Instant;

public record ReservationResponse(
        String reservationId,
        long cartVersion,
        String status,
        Instant expiresAt) {
}
