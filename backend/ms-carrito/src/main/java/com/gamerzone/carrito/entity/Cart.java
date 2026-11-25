package com.gamerzone.carrito.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "carts")
public class Cart {

    public static final String STATUS_ACTIVE = "ACTIVE";

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cart_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", length = 36, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "guest_token_hash", length = 64)
    private String guestTokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "version", nullable = false, precision = 10, scale = 0)
    private long version;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {
    }

    public Cart(String userId) {
        this.userId = userId;
        this.status = STATUS_ACTIVE;
        this.version = 0;
        this.lastActivityAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    @PrePersist
    void initialize() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (status == null) {
            status = STATUS_ACTIVE;
        }
        if (lastActivityAt == null) {
            lastActivityAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    public void addItem(CartItem item) {
        item.setCart(this);
        items.add(item);
    }

    public void incrementVersion() {
        version++;
        lastActivityAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void clear() {
        items.clear();
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getGuestTokenHash() { return guestTokenHash; }
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    public String getStatus() { return status; }
    public long getVersion() { return version; }
    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<CartItem> getItems() { return items; }
}
