package com.gamerzone.pedido.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Pedido sin pago (tabla {@code orders}, migración V1 de ms-pedido).
 *
 * <p>No guarda dirección ni importes: el total se calcula desde las líneas (snapshots inmutables).
 * La clave de idempotencia vive en {@link PedidoIdempotencia}. Las fechas se guardan en UTC.</p>
 */
@Entity
@Table(name = "orders")
public class Pedido {

    public static final String ESTADO_REGISTRADO_SIN_PAGO = "REGISTRADO_SIN_PAGO";
    public static final int POLITICA_PRECIOS_ACTUAL = 1;

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "order_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String customerId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "pricing_policy_version", nullable = false, precision = 5, scale = 0)
    private Integer pricingPolicyVersion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> items = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(String customerId) {
        this.customerId = customerId;
        this.status = ESTADO_REGISTRADO_SIN_PAGO;
        this.pricingPolicyVersion = POLITICA_PRECIOS_ACTUAL;
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void addItem(PedidoItem item) {
        items.add(item);
        item.setPedido(this);
    }

    @PrePersist
    void initialize() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    /** Total del pedido: suma de precio unitario por cantidad de cada línea. No se persiste. */
    public long getTotal() {
        long total = 0;
        for (PedidoItem item : items) {
            total = Math.addExact(total, item.getLineTotal());
        }
        return total;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getStatus() { return status; }
    public Integer getPricingPolicyVersion() { return pricingPolicyVersion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<PedidoItem> getItems() { return List.copyOf(items); }
}
