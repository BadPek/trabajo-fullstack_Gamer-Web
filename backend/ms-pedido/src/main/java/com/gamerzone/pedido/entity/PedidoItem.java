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

@Entity
@Table(name = "GZ_ORDER_ITEMS")
public class PedidoItem {

    @Id
    @Column(name = "ID", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Pedido pedido;

    @Column(name = "PRODUCT_ID", length = 100, nullable = false)
    private String productId;

    @Column(name = "PRODUCT_NAME", length = 300, nullable = false)
    private String productName;

    @Column(name = "QUANTITY", nullable = false)
    private int quantity;

    @Column(name = "UNIT_PRICE", nullable = false)
    private long unitPrice;

    @Column(name = "LINE_TOTAL", nullable = false)
    private long lineTotal;

    protected PedidoItem() {
    }

    public PedidoItem(String productId, String productName, int quantity, long unitPrice, long lineTotal) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
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
    public long getLineTotal() { return lineTotal; }
}
