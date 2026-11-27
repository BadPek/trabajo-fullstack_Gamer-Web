package com.gamerzone.auth.repository;

import java.util.Optional;
import java.util.List;

import com.gamerzone.auth.entity.RefreshTokenFamily;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenFamilyRepository extends JpaRepository<RefreshTokenFamily, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select family from RefreshTokenFamily family where family.familyId = :familyId")
    Optional<RefreshTokenFamily> findByIdForUpdate(@Param("familyId") String familyId);

    List<RefreshTokenFamily> findByUserIdAndRevokedAtIsNull(String userId);

    @Modifying
    @Query("delete from RefreshTokenFamily family where family.absoluteExpiresAt <= :now")
    int deleteExpired(@Param("now") java.time.LocalDateTime now);
}
