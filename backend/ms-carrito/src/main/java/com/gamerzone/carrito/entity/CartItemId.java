package com.gamerzone.carrito.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
public class CartItemId implements Serializable {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cart_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String cartId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "product_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String productId;

    protected CartItemId() {
    }

    public CartItemId(String cartId, String productId) {
        this.cartId = cartId;
        this.productId = productId;
    }

    public String getCartId() { return cartId; }
    public String getProductId() { return productId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CartItemId that)) {
            return false;
        }
        return Objects.equals(cartId, that.cartId) && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartId, productId);
    }
}
