package com.gamerzone.carrito.repository;

import java.util.Optional;

import com.gamerzone.carrito.entity.CartPriceQuote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartPriceQuoteRepository extends JpaRepository<CartPriceQuote, String> {

    Optional<CartPriceQuote> findByTokenHash(String tokenHash);
}
