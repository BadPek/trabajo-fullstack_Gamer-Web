package com.gamerzone.carrito.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cart_price_quotes")
public class CartPriceQuote {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "quote_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "cart_version", nullable = false, precision = 10, scale = 0)
    private long cartVersion;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "price_fingerprint", nullable = false, length = 64, columnDefinition = "CHAR(64)")
    private String priceFingerprint;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "token_hash", nullable = false, length = 64, columnDefinition = "CHAR(64)")
    private String tokenHash;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartQuoteItemSnapshotEntity> itemSnapshots = new ArrayList<>();

    @OneToOne(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private CartQuoteCouponSnapshotEntity couponSnapshot;

    protected CartPriceQuote() {
    }

    @PrePersist
    void initialize() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public CartPriceQuote(Cart cart, long cartVersion, String priceFingerprint,
            String tokenHash, LocalDateTime expiresAt) {
        this.id = UUID.randomUUID().toString();
        this.cart = cart;
        this.cartVersion = cartVersion;
        this.priceFingerprint = priceFingerprint;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public String getId() { return id; }
    public Cart getCart() { return cart; }
    public long getCartVersion() { return cartVersion; }
    public String getPriceFingerprint() { return priceFingerprint; }
    public String getTokenHash() { return tokenHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getConsumedAt() { return consumedAt; }
    public List<CartQuoteItemSnapshotEntity> getItemSnapshots() { return itemSnapshots; }
    public CartQuoteCouponSnapshotEntity getCouponSnapshot() { return couponSnapshot; }

    public void addItemSnapshot(CartQuoteItemSnapshotEntity snapshot) {
        itemSnapshots.add(snapshot);
    }

    public void setCouponSnapshot(CartQuoteCouponSnapshotEntity snapshot) {
        couponSnapshot = snapshot;
    }

    public void consume(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }

    public void setTokenHashForPersistence(String tokenHash) {
        this.tokenHash = tokenHash;
    }
}
