package com.gamerzone.carrito.dto.response;

import java.time.Instant;
import java.util.List;

public record CartQuoteResponse(
        long cartVersion,
        String priceQuoteToken,
        Instant expiresAt,
        List<CartQuoteItemSnapshot> items,
        CartQuoteCouponSnapshot couponSnapshot) {
}
