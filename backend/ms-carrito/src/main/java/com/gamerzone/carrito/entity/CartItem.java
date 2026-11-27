package com.gamerzone.carrito.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @EmbeddedId
    private CartItemId id;

    @MapsId("cartId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "quantity", nullable = false, precision = 3, scale = 0)
    private int quantity;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected CartItem() {
    }

    public CartItem(String productId, int quantity) {
        this.id = new CartItemId(null, productId);
        this.quantity = quantity;
    }

    void setCart(Cart cart) {
        this.cart = cart;
        if (id != null) {
            id = new CartItemId(cart.getId(), id.getProductId());
        }
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public CartItemId getId() { return id; }
    public Cart getCart() { return cart; }
    public String getProductId() { return id.getProductId(); }
    public int getQuantity() { return quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
