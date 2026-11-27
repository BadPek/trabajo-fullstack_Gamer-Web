package com.gamerzone.carrito.dto.response;

import java.time.Instant;

public record CartReservationResponse(
        String reservationId,
        long cartVersion,
        String status,
        Instant expiresAt) {
}
