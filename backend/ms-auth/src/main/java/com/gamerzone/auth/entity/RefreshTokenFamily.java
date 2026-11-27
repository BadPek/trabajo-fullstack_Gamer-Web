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
@Table(name = "refresh_token_families")
public class RefreshTokenFamily {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "family_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String familyId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", nullable = false, length = 36, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "absolute_expires_at", nullable = false)
    private LocalDateTime absoluteExpiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    protected RefreshTokenFamily() {
    }

    public RefreshTokenFamily(String userId, LocalDateTime createdAt, LocalDateTime absoluteExpiresAt) {
        this.familyId = UUID.randomUUID().toString();
        this.userId = userId;
        this.createdAt = createdAt;
        this.absoluteExpiresAt = absoluteExpiresAt;
    }

    public void revoke(LocalDateTime at) {
        if (revokedAt == null) {
            revokedAt = at;
        }
    }

    public String getFamilyId() { return familyId; }
    public String getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getAbsoluteExpiresAt() { return absoluteExpiresAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
}
