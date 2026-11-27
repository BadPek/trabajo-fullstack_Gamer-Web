package com.gamerzone.carrito.dto.response;

public record CartQuoteItemSnapshot(
        String productId,
        int quantity,
        String titleSnapshot,
        long unitPriceClp) {
}
