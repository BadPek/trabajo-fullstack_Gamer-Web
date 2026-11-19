package com.gamerzone.pedido.entity;

import java.time.Instant;
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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "GZ_ORDERS", uniqueConstraints = {
        @UniqueConstraint(name = "UK_GZ_ORDERS_IDEMPOTENCY", columnNames = {"CUSTOMER_ID", "IDEMPOTENCY_KEY"})
})
public class Pedido {

    @Id
    @Column(name = "ID", length = 36, nullable = false)
    private String id;

    @Column(name = "CUSTOMER_ID", length = 100, nullable = false)
    private String customerId;

    @Column(name = "IDEMPOTENCY_KEY", length = 128, nullable = false)
    private String idempotencyKey;

    @Column(name = "REQUEST_HASH", length = 64, nullable = false)
    private String requestHash;

    @Column(name = "STATUS", length = 30, nullable = false)
    private String status;

    @Column(name = "SHIPPING_ADDRESS", length = 300, nullable = false)
    private String shippingAddress;

    @Column(name = "CURRENCY", length = 3, nullable = false)
    private String currency;

    @Column(name = "TOTAL_AMOUNT", nullable = false)
    private long totalAmount;

    @Column(name = "CREATED_AT", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PedidoItem> items = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(String customerId, String idempotencyKey, String requestHash, String shippingAddress,
            long totalAmount) {
        this.customerId = customerId;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.shippingAddress = shippingAddress;
        this.totalAmount = totalAmount;
        this.currency = "CLP";
        this.status = "CREADO";
        this.createdAt = Instant.now();
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
            createdAt = Instant.now();
        }
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getRequestHash() { return requestHash; }
    public String getStatus() { return status; }
    public String getShippingAddress() { return shippingAddress; }
    public String getCurrency() { return currency; }
    public long getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public List<PedidoItem> getItems() { return List.copyOf(items); }
}
