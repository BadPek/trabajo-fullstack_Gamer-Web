package com.gamerzone.carrito.repository;

import java.util.Optional;

import com.gamerzone.carrito.entity.CartReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartReservationRepository extends JpaRepository<CartReservation, String> {

    Optional<CartReservation> findByCart_IdAndIdempotencyKey(String cartId, String idempotencyKey);

    Optional<CartReservation> findByIdAndCart_Id(String reservationId, String cartId);

    Optional<CartReservation> findByCart_IdAndStatus(String cartId, String status);
}
