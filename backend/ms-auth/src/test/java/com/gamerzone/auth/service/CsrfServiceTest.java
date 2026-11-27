package com.gamerzone.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.gamerzone.auth.entity.CsrfContext;
import com.gamerzone.auth.entity.CsrfToken;
import com.gamerzone.auth.exception.ApiException;
import com.gamerzone.auth.repository.CsrfContextRepository;
import com.gamerzone.auth.repository.CsrfTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CsrfServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-26T12:00:00Z");

    @Mock
    private CsrfContextRepository contextRepository;

    @Mock
    private CsrfTokenRepository tokenRepository;

    private CsrfService service;

    @BeforeEach
    void setUp() {
        service = new CsrfService(contextRepository, tokenRepository,
                Clock.fixed(NOW, ZoneOffset.UTC), new SecureRandom(), 7200, "https://gamerzone.example");
    }

    @Test
    void createsOpaqueContextAndStoresOnlyHashes() {
        when(contextRepository.save(any(CsrfContext.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenRepository.save(any(CsrfToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CsrfGrant grant = service.issue(null);

        ArgumentCaptor<CsrfContext> contextCaptor = ArgumentCaptor.forClass(CsrfContext.class);
        ArgumentCaptor<CsrfToken> tokenCaptor = ArgumentCaptor.forClass(CsrfToken.class);
        verify(contextRepository).save(contextCaptor.capture());
        verify(tokenRepository).save(tokenCaptor.capture());
        assertEquals(43, grant.contextCookie().length());
        assertEquals(43, grant.token().length());
        assertNotEquals(grant.contextCookie(), contextCaptor.getValue().getContextHash());
        assertNotEquals(grant.token(), tokenCaptor.getValue().getTokenHash());
        assertEquals(TokenHash.sha256(grant.contextCookie()), contextCaptor.getValue().getContextHash());
        assertEquals(TokenHash.sha256(grant.token()), tokenCaptor.getValue().getTokenHash());
        assertEquals(NOW.plusSeconds(7200), grant.tokenExpiresAt());
    }

    @Test
    void acceptsValidTokenOnlyForItsContextAndAllowedOrigin() {
        String contextValue = "csrf-context-value";
        String tokenValue = "csrf-token-value";
        LocalDateTime now = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);
        CsrfContext context = new CsrfContext(TokenHash.sha256(contextValue), now, now.plusHours(2));
        CsrfToken token = new CsrfToken(context.getContextId(), TokenHash.sha256(tokenValue), now, now.plusHours(2));
        when(contextRepository.findByContextHash(TokenHash.sha256(contextValue))).thenReturn(Optional.of(context));
        when(tokenRepository.findByTokenHash(TokenHash.sha256(tokenValue))).thenReturn(Optional.of(token));

        assertDoesNotThrow(() -> service.requireValid(contextValue, tokenValue, "https://gamerzone.example"));
    }

    @Test
    void rejectsUntrustedOriginBeforeLookingUpCsrfCredentials() {
        ApiException error = assertThrows(ApiException.class,
                () -> service.requireValid("context", "token", "https://attacker.example"));

        assertEquals("CSRF_INVALID", error.code());
    }

    @Test
    void rotatesAllTokensInAContextAndInvalidatesTheContextOnLogout() {
        String contextValue = "csrf-context-value";
        LocalDateTime now = LocalDateTime.ofInstant(NOW, ZoneOffset.UTC);
        CsrfContext context = new CsrfContext(TokenHash.sha256(contextValue), now, now.plusHours(2));
        CsrfToken token = new CsrfToken(context.getContextId(), TokenHash.sha256("csrf-token"), now, now.plusHours(2));
        when(contextRepository.findByContextHashForUpdate(TokenHash.sha256(contextValue)))
                .thenReturn(Optional.of(context));
        when(tokenRepository.findAllByContextIdAndRevokedAtIsNull(context.getContextId()))
                .thenReturn(List.of(token));

        service.rotate(contextValue);
        service.invalidate(contextValue);

        assertEquals(now, token.getRevokedAt());
        assertEquals(now, context.getExpiresAt());
        verify(tokenRepository, org.mockito.Mockito.times(2))
                .findAllByContextIdAndRevokedAtIsNull(context.getContextId());
    }
}
