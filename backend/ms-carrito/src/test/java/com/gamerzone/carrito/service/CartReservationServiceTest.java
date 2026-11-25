package com.gamerzone.carrito.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.Optional;

import com.gamerzone.carrito.dto.request.CrearReservaRequest;
import com.gamerzone.carrito.entity.Cart;
import com.gamerzone.carrito.entity.CartReservation;
import com.gamerzone.carrito.entity.CartItem;
import com.gamerzone.carrito.entity.Coupon;
import com.gamerzone.carrito.repository.CartRepository;
import com.gamerzone.carrito.repository.CartReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CartReservationServiceTest {

    private static final String USER_ID = "11111111-1111-4111-8111-111111111111";
    private static final String CART_ID = "22222222-2222-4222-8222-222222222222";
    private static final String IDEMPOTENCY_KEY = "33333333-3333-4333-8333-333333333333";

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartReservationRepository reservationRepository;

    @Test
    void createReservationUsesFiveMinuteExpirationAndCartVersion() {
        Cart cart = new Cart(USER_ID);
        ReflectionTestUtils.setField(cart, "id", CART_ID);
        when(cartRepository.findByUserIdAndStatus(USER_ID, Cart.STATUS_ACTIVE))
                .thenReturn(Optional.of(cart));
        when(reservationRepository.findByCart_IdAndIdempotencyKey(CART_ID, IDEMPOTENCY_KEY))
                .thenReturn(Optional.empty());
        when(reservationRepository.findByCart_IdAndStatus(CART_ID, CartReservation.STATUS_ACTIVE))
                .thenReturn(Optional.empty());
        when(reservationRepository.saveAndFlush(any(CartReservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CartReservationService service = new CartReservationService(
                cartRepository,
                reservationRepository);

        var response = service.create(USER_ID,
                new CrearReservaRequest(0L, IDEMPOTENCY_KEY));

        assertEquals(0, response.cartVersion());
        assertEquals(CartReservation.STATUS_ACTIVE, response.status());
        assertNotNull(response.expiresAt());
        verify(reservationRepository).saveAndFlush(any(CartReservation.class));
    }

    @Test
    void completingReservationClearsCartCouponAndIncreasesVersionOnce() {
        Cart cart = new Cart(USER_ID);
        ReflectionTestUtils.setField(cart, "id", CART_ID);
        cart.addItem(new CartItem("550e8400-e29b-41d4-a716-446655440000", 2));
        Coupon coupon = org.mockito.Mockito.mock(Coupon.class);
        cart.setCoupon(coupon);

        CartReservation reservation = new CartReservation(
                cart,
                7,
                IDEMPOTENCY_KEY,
                java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(5));
        ReflectionTestUtils.setField(reservation, "id", "44444444-4444-4444-8444-444444444444");

        when(cartRepository.findByUserIdAndStatus(USER_ID, Cart.STATUS_ACTIVE))
                .thenReturn(Optional.of(cart));
        when(reservationRepository.findByIdAndCart_Id(
                "44444444-4444-4444-8444-444444444444", CART_ID))
                .thenReturn(Optional.of(reservation));
        when(cartRepository.saveAndFlush(cart)).thenReturn(cart);
        when(reservationRepository.saveAndFlush(reservation)).thenReturn(reservation);

        CartReservationService service = new CartReservationService(
                cartRepository,
                reservationRepository);

        service.complete(USER_ID, "44444444-4444-4444-8444-444444444444");
        service.complete(USER_ID, "44444444-4444-4444-8444-444444444444");

        assertEquals(0, cart.getItems().size());
        assertEquals(null, cart.getCoupon());
        assertEquals(1, cart.getVersion());
        verify(cartRepository, times(1)).saveAndFlush(cart);
    }
}
