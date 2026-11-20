package com.gamerzone.pedido.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Clave primaria de {@code order_idempotency}: cliente + Idempotency-Key. */
@Embeddable
public class PedidoIdempotenciaId implements Serializable {

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String customerId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "idempotency_key", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String idempotencyKey;

    protected PedidoIdempotenciaId() {
    }

    public PedidoIdempotenciaId(String customerId, String idempotencyKey) {
        this.customerId = customerId;
        this.idempotencyKey = idempotencyKey;
    }

    public String getCustomerId() { return customerId; }
    public String getIdempotencyKey() { return idempotencyKey; }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PedidoIdempotenciaId that)) {
            return false;
        }
        return Objects.equals(customerId, that.customerId) && Objects.equals(idempotencyKey, that.idempotencyKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, idempotencyKey);
    }
}
