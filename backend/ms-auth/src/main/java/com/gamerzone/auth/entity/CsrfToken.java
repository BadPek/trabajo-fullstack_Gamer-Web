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
@Table(name = "csrf_tokens")
public class CsrfToken {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "token_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String tokenId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "context_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String contextId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "token_hash", nullable = false, length = 64, columnDefinition = "CHAR(64)")
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    protected CsrfToken() {
    }

    public CsrfToken(String contextId, String tokenHash, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.tokenId = UUID.randomUUID().toString();
        this.contextId = contextId;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public void revoke(LocalDateTime at) {
        if (revokedAt == null) {
            revokedAt = at;
        }
    }

    public String getTokenId() { return tokenId; }
    public String getContextId() { return contextId; }
    public String getTokenHash() { return tokenHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
}
