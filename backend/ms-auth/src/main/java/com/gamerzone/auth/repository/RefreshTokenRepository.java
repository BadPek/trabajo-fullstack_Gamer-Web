package com.gamerzone.auth.repository;

import java.util.Optional;

import com.gamerzone.auth.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token where token.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("update RefreshToken token set token.revokedAt = :revokedAt "
            + "where token.familyId = :familyId and token.revokedAt is null")
    int revokeActiveByFamilyId(@Param("familyId") String familyId,
            @Param("revokedAt") java.time.LocalDateTime revokedAt);

    @Modifying
    @Query("delete from RefreshToken token where token.familyId in "
            + "(select family.familyId from RefreshTokenFamily family where family.absoluteExpiresAt <= :now)")
    int deleteForExpiredFamilies(@Param("now") java.time.LocalDateTime now);
}
