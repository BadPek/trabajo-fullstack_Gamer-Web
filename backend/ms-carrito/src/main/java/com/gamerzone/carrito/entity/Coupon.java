package com.gamerzone.carrito.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "coupon_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "discount_percent", nullable = false, precision = 3, scale = 0)
    private int discountPercent;

    @Column(name = "active", nullable = false, precision = 1, scale = 0)
    private int active;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected Coupon() {
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public int getDiscountPercent() { return discountPercent; }
    public boolean isActive() { return active == 1; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
