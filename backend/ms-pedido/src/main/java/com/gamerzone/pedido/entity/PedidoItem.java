package com.gamerzone.pedido.entity;

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

/**
 * Línea de pedido (tabla {@code order_items}): snapshot del título y del precio unitario al
 * confirmar. El subtotal de la línea se calcula y no se guarda.
 */
@Entity
@Table(name = "order_items")
public class PedidoItem {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "order_item_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Pedido pedido;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "product_id", length = 36, columnDefinition = "CHAR(36)")
    private String productId;

    @Column(name = "title_snapshot", nullable = false, length = 150)
    private String productName;

    @Column(name = "unit_price_clp", nullable = false, precision = 10, scale = 0)
    private long unitPrice;

    @Column(name = "quantity", nullable = false, precision = 3, scale = 0)
    private int quantity;

    protected PedidoItem() {
    }

    public PedidoItem(String productId, String productName, int quantity, long unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    @PrePersist
    void assignId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public long getUnitPrice() { return unitPrice; }

    /** Precio unitario por cantidad. */
    public long getLineTotal() {
        return Math.multiplyExact(unitPrice, (long) quantity);
    }
}
