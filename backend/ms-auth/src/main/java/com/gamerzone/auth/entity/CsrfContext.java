package com.gamerzone.auth.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "csrf_contexts")
public class CsrfContext {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "context_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String contextId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "context_hash", nullable = false, length = 64, columnDefinition = "CHAR(64)")
    private String contextHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    protected CsrfContext() {
    }

    public CsrfContext(String contextHash, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.contextId = UUID.randomUUID().toString();
        this.contextHash = contextHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public void expire(LocalDateTime at) {
        if (expiresAt.isAfter(at)) {
            expiresAt = at;
        }
    }

    public String getContextId() { return contextId; }
    public String getContextHash() { return contextHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
