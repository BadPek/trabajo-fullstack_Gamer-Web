package com.gamerzone.carrito.repository;

import com.gamerzone.carrito.entity.CartItem;
import com.gamerzone.carrito.entity.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {
}
