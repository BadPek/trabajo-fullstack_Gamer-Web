package com.gamerzone.carrito.repository;

import java.util.Optional;

import com.gamerzone.carrito.entity.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface CartRepository extends JpaRepository<Cart, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Cart> findByUserIdAndStatus(String userId, String status);
}
