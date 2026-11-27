package com.gamerzone.auth.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import com.gamerzone.auth.entity.RefreshToken;
import com.gamerzone.auth.entity.RefreshTokenFamily;
import com.gamerzone.auth.entity.Usuario;
import com.gamerzone.auth.exception.ApiException;
import com.gamerzone.auth.repository.RefreshTokenFamilyRepository;
import com.gamerzone.auth.repository.RefreshTokenRepository;
import com.gamerzone.auth.repository.UsuarioRepository;
import java.security.SecureRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenFamilyRepository familyRepository;
    private final RefreshTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;
    private final SecureRandom secureRandom;
    private final Duration idleLifetime;
    private final Duration absoluteLifetime;

    public RefreshTokenService(
            RefreshTokenFamilyRepository familyRepository,
            RefreshTokenRepository tokenRepository,
            UsuarioRepository usuarioRepository,
            Clock clock,
            SecureRandom secureRandom,
            @Value("${auth.session.refresh-idle-days:7}") long idleDays,
            @Value("${auth.session.refresh-absolute-days:30}") long absoluteDays) {
        if (idleDays <= 0 || absoluteDays < idleDays) {
            throw new IllegalArgumentException("La vigencia absoluta de refresh debe cubrir la vigencia inactiva.");
        }
        this.familyRepository = familyRepository;
        this.tokenRepository = tokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
        this.secureRandom = secureRandom;
        this.idleLifetime = Duration.ofDays(idleDays);
        this.absoluteLifetime = Duration.ofDays(absoluteDays);
    }

    @Transactional
    public RefreshTokenGrant createSession(String userId) {
        Instant now = clock.instant();
        LocalDateTime createdAt = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
        Instant absoluteExpiresAt = now.plus(absoluteLifetime);
        RefreshTokenFamily family = familyRepository.save(new RefreshTokenFamily(
                userId,
                createdAt,
                LocalDateTime.ofInstant(absoluteExpiresAt, ZoneOffset.UTC)));
        String rawToken = TokenHash.generate(secureRandom);
        Instant tokenExpiresAt = min(now.plus(idleLifetime), absoluteExpiresAt);
        tokenRepository.save(new RefreshToken(
                family.getFamilyId(),
                TokenHash.sha256(rawToken),
                createdAt,
                LocalDateTime.ofInstant(tokenExpiresAt, ZoneOffset.UTC)));
        return new RefreshTokenGrant(rawToken, tokenExpiresAt);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public RefreshSessionGrant rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw invalidSession();
        }
        String tokenHash = TokenHash.sha256(rawToken);
        Optional<RefreshToken> tokenLookup = tokenRepository.findByTokenHash(tokenHash);
        if (tokenLookup.isEmpty()) {
            throw invalidSession();
        }
        RefreshTokenFamily family = familyRepository.findByIdForUpdate(tokenLookup.get().getFamilyId())
                .orElseThrow(RefreshTokenService::invalidSession);
        RefreshToken token = tokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(RefreshTokenService::invalidSession);
        LocalDateTime now = nowUtc();

        if (token.getRevokedAt() != null || family.getRevokedAt() != null) {
            revokeFamily(family, now);
            throw invalidSession();
        }
        if (!token.getExpiresAt().isAfter(now) || !family.getAbsoluteExpiresAt().isAfter(now)) {
            revokeFamily(family, now);
            throw invalidSession();
        }

        Usuario usuario = usuarioRepository.findById(family.getUserId())
                .filter(Usuario::isActive)
                .orElseGet(() -> {
                    revokeFamily(family, now);
                    throw invalidSession();
                });

        token.revoke(now);
        String nextRawToken = TokenHash.generate(secureRandom);
        Instant absoluteExpiry = family.getAbsoluteExpiresAt().toInstant(ZoneOffset.UTC);
        Instant nextExpiresAt = min(clock.instant().plus(idleLifetime), absoluteExpiry);
        tokenRepository.save(new RefreshToken(
                family.getFamilyId(),
                TokenHash.sha256(nextRawToken),
                now,
                LocalDateTime.ofInstant(nextExpiresAt, ZoneOffset.UTC)));
        return new RefreshSessionGrant(usuario, nextRawToken, nextExpiresAt);
    }

    @Transactional
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        String tokenHash = TokenHash.sha256(rawToken);
        Optional<RefreshToken> tokenLookup = tokenRepository.findByTokenHash(tokenHash);
        if (tokenLookup.isEmpty()) {
            return;
        }
        Optional<RefreshTokenFamily> familyLookup = familyRepository.findByIdForUpdate(
                tokenLookup.get().getFamilyId());
        if (familyLookup.isEmpty()) {
            return;
        }
        RefreshTokenFamily family = familyLookup.get();
        LocalDateTime now = nowUtc();
        family.revoke(now);
        tokenRepository.revokeActiveByFamilyId(family.getFamilyId(), now);
    }

    @Transactional
    public void revokeAllForUser(String userId) {
        LocalDateTime now = nowUtc();
        for (RefreshTokenFamily family : familyRepository.findByUserIdAndRevokedAtIsNull(userId)) {
            family.revoke(now);
            tokenRepository.revokeActiveByFamilyId(family.getFamilyId(), now);
        }
    }

    private void revokeFamily(RefreshTokenFamily family, LocalDateTime now) {
        family.revoke(now);
        tokenRepository.revokeActiveByFamilyId(family.getFamilyId(), now);
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private static Instant min(Instant first, Instant second) {
        return first.isBefore(second) ? first : second;
    }

    private static ApiException invalidSession() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_SESSION",
                "La sesión no es válida o expiró. Inicia sesión nuevamente.");
    }
}
