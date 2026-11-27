package com.gamerzone.carrito.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cart_quote_items")
public class CartQuoteItemSnapshotEntity {

    @EmbeddedId
    private Id id;

    @MapsId("quoteId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quote_id", nullable = false)
    private CartPriceQuote quote;

    @Column(name = "quantity", nullable = false, precision = 3, scale = 0)
    private int quantity;

    @Column(name = "title_snapshot", nullable = false, length = 150)
    private String titleSnapshot;

    @Column(name = "unit_price_clp", nullable = false, precision = 10, scale = 0)
    private long unitPriceClp;

    protected CartQuoteItemSnapshotEntity() {
    }

    public CartQuoteItemSnapshotEntity(
            CartPriceQuote quote,
            String productId,
            int quantity,
            String titleSnapshot,
            long unitPriceClp) {
        this.quote = quote;
        this.id = new Id(quote.getId(), productId);
        this.quantity = quantity;
        this.titleSnapshot = titleSnapshot;
        this.unitPriceClp = unitPriceClp;
    }

    public Id getId() { return id; }
    public CartPriceQuote getQuote() { return quote; }
    public String getProductId() { return id.getProductId(); }
    public int getQuantity() { return quantity; }
    public String getTitleSnapshot() { return titleSnapshot; }
    public long getUnitPriceClp() { return unitPriceClp; }

    @Embeddable
    public static class Id implements Serializable {

        @JdbcTypeCode(SqlTypes.CHAR)
        @Column(name = "quote_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
        private String quoteId;

        @JdbcTypeCode(SqlTypes.CHAR)
        @Column(name = "product_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
        private String productId;

        protected Id() {
        }

        public Id(String quoteId, String productId) {
            this.quoteId = quoteId;
            this.productId = productId;
        }

        public String getQuoteId() { return quoteId; }
        public String getProductId() { return productId; }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Id that)) {
                return false;
            }
            return Objects.equals(quoteId, that.quoteId)
                    && Objects.equals(productId, that.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(quoteId, productId);
        }
    }
}
