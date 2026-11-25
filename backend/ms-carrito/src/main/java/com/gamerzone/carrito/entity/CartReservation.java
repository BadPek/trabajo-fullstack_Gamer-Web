package com.gamerzone.carrito.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cart_reservations")
public class CartReservation {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_RELEASED = "RELEASED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "reservation_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "cart_version", nullable = false, precision = 10, scale = 0)
    private long cartVersion;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "idempotency_key", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String idempotencyKey;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected CartReservation() {
    }

    public CartReservation(Cart cart, long cartVersion, String idempotencyKey,
            LocalDateTime expiresAt) {
        this.cart = cart;
        this.cartVersion = cartVersion;
        this.idempotencyKey = idempotencyKey;
        this.status = STATUS_ACTIVE;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void initialize() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }

    public void complete() {
        this.status = STATUS_COMPLETED;
        this.completedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void release() {
        this.status = STATUS_RELEASED;
    }

    public void expire() {
        this.status = STATUS_EXPIRED;
    }

    public String getId() { return id; }
    public Cart getCart() { return cart; }
    public long getCartVersion() { return cartVersion; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
