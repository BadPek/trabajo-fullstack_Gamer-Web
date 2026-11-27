package com.gamerzone.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.security.SecureRandom;

import com.gamerzone.auth.entity.RefreshToken;
import com.gamerzone.auth.entity.RefreshTokenFamily;
import com.gamerzone.auth.entity.Usuario;
import com.gamerzone.auth.exception.ApiException;
import com.gamerzone.auth.repository.RefreshTokenFamilyRepository;
import com.gamerzone.auth.repository.RefreshTokenRepository;
import com.gamerzone.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-26T12:00:00Z");

    @Mock
    private RefreshTokenFamilyRepository familyRepository;

    @Mock
    private RefreshTokenRepository tokenRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(familyRepository, tokenRepository, usuarioRepository,
                Clock.fixed(NOW, ZoneOffset.UTC), new SecureRandom(), 7, 30);
    }

    @Test
    void storesOnlyRefreshHashAndLimitsInactivityExpiry() {
        when(familyRepository.save(any(RefreshTokenFamily.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenGrant grant = service.createSession("user-id");

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertNotEquals(grant.refreshToken(), tokenCaptor.getValue().getTokenHash());
        assertEquals(TokenHash.sha256(grant.refreshToken()), tokenCaptor.getValue().getTokenHash());
        assertEquals(NOW.plusSeconds(7 * 24 * 60 * 60), grant.expiresAt());
    }

    @Test
    void rotatesRefreshTokenAndRevokesThePreviousOne() {
        String oldRawToken = "old-refresh-token";
        LocalDateTime now = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);
        Usuario user = new Usuario("190110222", "Ana", "Gamer", "ana@gmail.com", "hash", null, "Calle 1");
        RefreshTokenFamily family = new RefreshTokenFamily(user.getId(), now, now.plusDays(30));
        RefreshToken oldToken = new RefreshToken(family.getFamilyId(), TokenHash.sha256(oldRawToken),
                now, now.plusDays(7));
        when(tokenRepository.findByTokenHash(TokenHash.sha256(oldRawToken))).thenReturn(Optional.of(oldToken));
        when(familyRepository.findByIdForUpdate(family.getFamilyId())).thenReturn(Optional.of(family));
        when(tokenRepository.findByTokenHashForUpdate(TokenHash.sha256(oldRawToken))).thenReturn(Optional.of(oldToken));
        when(usuarioRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshSessionGrant grant = service.rotate(oldRawToken);

        ArgumentCaptor<RefreshToken> newTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(tokenRepository).save(newTokenCaptor.capture());
        assertEquals(now, oldToken.getRevokedAt());
        assertEquals(user.getId(), grant.usuario().getId());
        assertNotEquals(oldRawToken, grant.refreshToken());
        assertEquals(TokenHash.sha256(grant.refreshToken()), newTokenCaptor.getValue().getTokenHash());
        assertEquals(now.plusDays(7), newTokenCaptor.getValue().getExpiresAt());
    }

    @Test
    void replayingRevokedTokenRevokesItsEntireFamily() {
        String rawToken = "replayed-refresh-token";
        LocalDateTime now = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);
        RefreshTokenFamily family = new RefreshTokenFamily("user-id", now.minusHours(1), now.plusDays(29));
        RefreshToken token = new RefreshToken(family.getFamilyId(), TokenHash.sha256(rawToken),
                now.minusHours(1), now.plusDays(6));
        token.revoke(now.minusMinutes(1));
        when(tokenRepository.findByTokenHash(TokenHash.sha256(rawToken))).thenReturn(Optional.of(token));
        when(familyRepository.findByIdForUpdate(family.getFamilyId())).thenReturn(Optional.of(family));
        when(tokenRepository.findByTokenHashForUpdate(TokenHash.sha256(rawToken))).thenReturn(Optional.of(token));

        ApiException error = assertThrows(ApiException.class, () -> service.rotate(rawToken));

        assertEquals("INVALID_SESSION", error.code());
        assertEquals(now, family.getRevokedAt());
        verify(tokenRepository).revokeActiveByFamilyId(eq(family.getFamilyId()), any(LocalDateTime.class));
    }

    @Test
    void rotationNeverExtendsFamilyBeyondItsAbsoluteExpiry() {
        String oldRawToken = "near-expiry-refresh-token";
        LocalDateTime now = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);
        Usuario user = new Usuario("190110222", "Ana", "Gamer", "ana@gmail.com", "hash", null, "Calle 1");
        RefreshTokenFamily family = new RefreshTokenFamily(
                user.getId(), now.minusDays(29), now.plusDays(1));
        RefreshToken oldToken = new RefreshToken(family.getFamilyId(), TokenHash.sha256(oldRawToken),
                now.minusHours(1), now.plusDays(6));
        when(tokenRepository.findByTokenHash(TokenHash.sha256(oldRawToken))).thenReturn(Optional.of(oldToken));
        when(familyRepository.findByIdForUpdate(family.getFamilyId())).thenReturn(Optional.of(family));
        when(tokenRepository.findByTokenHashForUpdate(TokenHash.sha256(oldRawToken))).thenReturn(Optional.of(oldToken));
        when(usuarioRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshSessionGrant grant = service.rotate(oldRawToken);

        assertEquals(NOW.plusSeconds(24 * 60 * 60), grant.expiresAt());
    }
}
