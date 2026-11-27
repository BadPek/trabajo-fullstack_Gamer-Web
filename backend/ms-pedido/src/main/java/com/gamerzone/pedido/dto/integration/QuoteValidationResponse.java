package com.gamerzone.pedido.dto.integration;

import java.time.Instant;
import java.util.List;

public record QuoteValidationResponse(
        long cartVersion,
        Instant expiresAt,
        List<QuoteItemSnapshot> items,
        QuoteCouponSnapshot couponSnapshot) {

    public record QuoteItemSnapshot(
            String productId,
            int quantity,
            String titleSnapshot,
            long unitPriceClp) {
    }

    public record QuoteCouponSnapshot(
            String code,
            int discountPercent) {
    }
}
