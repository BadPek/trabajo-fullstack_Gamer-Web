package com.gamerzone.carrito.dto.response;

import java.time.Instant;
import java.util.List;

public record ValidatedQuoteResponse(
        long cartVersion,
        Instant expiresAt,
        List<CartQuoteItemSnapshot> items,
        CartQuoteCouponSnapshot couponSnapshot) {
}
