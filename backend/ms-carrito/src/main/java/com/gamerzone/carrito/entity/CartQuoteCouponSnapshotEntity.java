package com.gamerzone.carrito.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_quote_coupon_snapshots")
public class CartQuoteCouponSnapshotEntity {

    @Id
    @Column(name = "quote_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String quoteId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_id", nullable = false)
    private CartPriceQuote quote;

    @Column(name = "coupon_code_snapshot", nullable = false, length = 30)
    private String code;

    @Column(name = "discount_percent_snapshot", nullable = false, precision = 3, scale = 0)
    private int discountPercent;

    protected CartQuoteCouponSnapshotEntity() {
    }

    public CartQuoteCouponSnapshotEntity(
            CartPriceQuote quote,
            String code,
            int discountPercent) {
        this.quote = quote;
        this.quoteId = quote.getId();
        this.code = code;
        this.discountPercent = discountPercent;
    }

    public String getQuoteId() { return quoteId; }
    public String getCode() { return code; }
    public int getDiscountPercent() { return discountPercent; }
}
