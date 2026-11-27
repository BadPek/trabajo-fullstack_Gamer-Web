package com.gamerzone.auth.repository;

import java.util.List;
import java.util.Optional;

import com.gamerzone.auth.entity.CsrfToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CsrfTokenRepository extends JpaRepository<CsrfToken, String> {

    Optional<CsrfToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from CsrfToken token where token.tokenHash = :tokenHash")
    Optional<CsrfToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    List<CsrfToken> findAllByContextIdAndRevokedAtIsNull(String contextId);

    @Modifying
    @Query("delete from CsrfToken token where token.expiresAt <= :now")
    int deleteExpired(@Param("now") java.time.LocalDateTime now);
}
