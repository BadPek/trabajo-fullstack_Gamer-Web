package com.gamerzone.pedido.integration;

import com.gamerzone.pedido.dto.integration.CarritoResponse;
import com.gamerzone.pedido.dto.integration.QuoteValidationResponse;
import com.gamerzone.pedido.dto.integration.ReservationResponse;

public interface CarritoClient {

    CarritoResponse getCurrentCart(String bearerToken);

    QuoteValidationResponse validateQuote(String bearerToken, long cartVersion, String priceQuoteToken);

    void consumeQuote(String bearerToken, long cartVersion, String priceQuoteToken);

    ReservationResponse createReservation(String bearerToken, long cartVersion, String idempotencyKey);

    ReservationResponse completeReservation(String bearerToken, String reservationId);

    void releaseReservation(String bearerToken, String reservationId);
}
