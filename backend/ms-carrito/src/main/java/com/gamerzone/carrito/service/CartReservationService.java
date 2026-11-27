package com.gamerzone.carrito.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

import com.gamerzone.carrito.dto.request.CrearReservaRequest;
import com.gamerzone.carrito.dto.response.CartReservationResponse;
import com.gamerzone.carrito.entity.Cart;
import com.gamerzone.carrito.entity.CartReservation;
import com.gamerzone.carrito.repository.CartRepository;
import com.gamerzone.carrito.repository.CartReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartReservationService {

    private static final long TTL_SECONDS = 5 * 60;

    private final CartRepository cartRepository;
    private final CartReservationRepository reservationRepository;

    public CartReservationService(
            CartRepository cartRepository,
            CartReservationRepository reservationRepository) {
        this.cartRepository = cartRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public CartReservationResponse create(String customerId, CrearReservaRequest request) {
        String userId = normalizeUuid(customerId);
        String idempotencyKey = request.idempotencyKey().trim().toLowerCase(Locale.ROOT);
        Cart cart = cartRepository.findByUserIdAndStatus(userId, Cart.STATUS_ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El carrito activo no existe."));
        if (request.cartVersion() != cart.getVersion()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La versión del carrito no coincide.");
        }

        var existing = reservationRepository.findByCart_IdAndIdempotencyKey(
                cart.getId(), idempotencyKey);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        var active = reservationRepository.findByCart_IdAndStatus(
                cart.getId(), CartReservation.STATUS_ACTIVE);
        if (active.isPresent()) {
            CartReservation activeReservation = active.get();
            if (activeReservation.isExpired(LocalDateTime.now(ZoneOffset.UTC))) {
                activeReservation.expire();
                reservationRepository.saveAndFlush(activeReservation);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El carrito ya tiene una reserva activa.");
            }
        }

        Instant expiresAt = Instant.now().plusSeconds(TTL_SECONDS);
        CartReservation reservation = new CartReservation(
                cart,
                cart.getVersion(),
                idempotencyKey,
                LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
        reservationRepository.saveAndFlush(reservation);
        return toResponse(reservation);
    }

    @Transactional
    public CartReservationResponse complete(String customerId, String reservationId) {
        CartReservation reservation = findOwnReservation(customerId, reservationId);
        if (reservation.isExpired(LocalDateTime.now(ZoneOffset.UTC))
                && CartReservation.STATUS_ACTIVE.equals(reservation.getStatus())) {
            reservation.expire();
            reservationRepository.saveAndFlush(reservation);
            throw new ResponseStatusException(
                    HttpStatus.GONE,
                    "La reserva expiró.");
        }
        if (!CartReservation.STATUS_ACTIVE.equals(reservation.getStatus())) {
            if (CartReservation.STATUS_COMPLETED.equals(reservation.getStatus())) {
                return toResponse(reservation);
            }
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La reserva ya no está activa.");
        }
        reservation.complete();
        Cart cart = reservation.getCart();
        cart.clear();
        cart.setCoupon(null);
        cart.incrementVersion();
        cartRepository.saveAndFlush(cart);
        reservationRepository.saveAndFlush(reservation);
        return toResponse(reservation);
    }

    @Transactional
    public void release(String customerId, String reservationId) {
        CartReservation reservation = findOwnReservation(customerId, reservationId);
        if (CartReservation.STATUS_ACTIVE.equals(reservation.getStatus())) {
            reservation.release();
            reservationRepository.saveAndFlush(reservation);
        }
    }

    private CartReservation findOwnReservation(String customerId, String reservationId) {
        String userId = normalizeUuid(customerId);
        Cart cart = cartRepository.findByUserIdAndStatus(userId, Cart.STATUS_ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El carrito activo no existe."));
        return reservationRepository.findByIdAndCart_Id(reservationId, cart.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La reserva no existe."));
    }

    private static CartReservationResponse toResponse(CartReservation reservation) {
        Instant expiresAt = reservation.getExpiresAt().toInstant(ZoneOffset.UTC);
        return new CartReservationResponse(
                reservation.getId(),
                reservation.getCartVersion(),
                reservation.getStatus(),
                expiresAt);
    }

    private static String normalizeUuid(String value) {
        try {
            return UUID.fromString(value).toString().toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El identificador UUID no es válido.",
                    exception);
        }
    }
}
