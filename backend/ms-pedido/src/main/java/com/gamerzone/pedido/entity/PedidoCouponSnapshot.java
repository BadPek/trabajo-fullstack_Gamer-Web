package com.gamerzone.pedido.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "order_coupon_snapshots")
public class PedidoCouponSnapshot {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "order_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String orderId;

    @MapsId
    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Pedido pedido;

    @Column(name = "coupon_code_snapshot", nullable = false, length = 30)
    private String code;

    @Column(name = "discount_percent_snapshot", nullable = false, precision = 3, scale = 0)
    private int discountPercent;

    protected PedidoCouponSnapshot() {
    }

    public PedidoCouponSnapshot(Pedido pedido, String code, int discountPercent) {
        this.pedido = pedido;
        this.orderId = pedido.getId();
        this.code = code;
        this.discountPercent = discountPercent;
    }

    void setPedido(Pedido pedido) {
        this.pedido = pedido;
        this.orderId = pedido.getId();
    }

    public String getCode() { return code; }
    public int getDiscountPercent() { return discountPercent; }
}
