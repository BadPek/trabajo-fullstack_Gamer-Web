package com.gamerzone.pedido.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Registro de idempotencia de {@code POST /api/v1/orders} (tabla {@code order_idempotency}).
 *
 * <p>Una clave por cliente y un pedido por clave. Un reintento con la misma versión del carrito y
 * la misma cotización devuelve el pedido original; con otros datos se rechaza con 409.</p>
 */
@Entity
@Table(name = "order_idempotency")
public class PedidoIdempotencia {

    @EmbeddedId
    private PedidoIdempotenciaId id;

    @Column(name = "cart_version", nullable = false, precision = 10, scale = 0)
    private Long cartVersion;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cart_fingerprint", nullable = false, length = 64, columnDefinition = "CHAR(64)")
    private String cartFingerprint;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "order_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String orderId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "reservation_id", length = 36, columnDefinition = "CHAR(36)")
    private String reservationId;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PedidoIdempotencia() {
    }

    public PedidoIdempotencia(String customerId, String idempotencyKey, long cartVersion,
            String cartFingerprint, String orderId) {
        this(customerId, idempotencyKey, cartVersion, cartFingerprint, orderId, null);
    }

    public PedidoIdempotencia(String customerId, String idempotencyKey, long cartVersion,
            String cartFingerprint, String orderId, String reservationId) {
        this.id = new PedidoIdempotenciaId(customerId, idempotencyKey);
        this.cartVersion = cartVersion;
        this.cartFingerprint = cartFingerprint;
        this.orderId = orderId;
        this.reservationId = reservationId;
    }

    /** {@code true} si el reintento trae la misma versión del carrito y la misma cotización. */
    public boolean matches(long version, String fingerprint) {
        return cartVersion != null && cartVersion == version && cartFingerprint.equals(fingerprint);
    }

    public PedidoIdempotenciaId getId() { return id; }
    public Long getCartVersion() { return cartVersion; }
    public String getCartFingerprint() { return cartFingerprint; }
    public String getOrderId() { return orderId; }
    public String getReservationId() { return reservationId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
