package com.gamerzone.auth.repository;

import java.util.Optional;

import com.gamerzone.auth.entity.CsrfContext;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface CsrfContextRepository extends JpaRepository<CsrfContext, String> {

    Optional<CsrfContext> findByContextHash(String contextHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select context from CsrfContext context where context.contextHash = :contextHash")
    Optional<CsrfContext> findByContextHashForUpdate(@Param("contextHash") String contextHash);

    @Modifying
    @Query("delete from CsrfContext context where context.expiresAt <= :now")
    int deleteExpired(@Param("now") java.time.LocalDateTime now);
}
