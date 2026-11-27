package com.gamerzone.auth.service;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.security.SecureRandom;

import com.gamerzone.auth.entity.CsrfContext;
import com.gamerzone.auth.entity.CsrfToken;
import com.gamerzone.auth.exception.ApiException;
import com.gamerzone.auth.repository.CsrfContextRepository;
import com.gamerzone.auth.repository.CsrfTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CsrfService {

    public static final String CONTEXT_COOKIE = "gz_csrf_context";
    public static final String TOKEN_HEADER = "X-CSRF-TOKEN";

    private final CsrfContextRepository contextRepository;
    private final CsrfTokenRepository tokenRepository;
    private final Clock clock;
    private final SecureRandom secureRandom;
    private final Duration lifetime;
    private final List<String> allowedOrigins;

    public CsrfService(
            CsrfContextRepository contextRepository,
            CsrfTokenRepository tokenRepository,
            Clock clock,
            SecureRandom secureRandom,
            @Value("${auth.csrf.expiration-seconds:7200}") long expirationSeconds,
            @Value("${auth.frontend-origin:http://localhost:5173}") String frontendOrigin) {
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("La vigencia CSRF debe ser positiva.");
        }
        this.contextRepository = contextRepository;
        this.tokenRepository = tokenRepository;
        this.clock = clock;
        this.secureRandom = secureRandom;
        this.lifetime = Duration.ofSeconds(expirationSeconds);
        this.allowedOrigins = List.of("http://localhost:5173", frontendOrigin.trim()).stream()
                .distinct()
                .toList();
    }

    @Transactional
    public CsrfGrant issue(String rawContext) {
        LocalDateTime now = nowUtc();
        Optional<CsrfContext> existing = Optional.empty();
        if (rawContext != null && !rawContext.isBlank()) {
            existing = contextRepository.findByContextHashForUpdate(TokenHash.sha256(rawContext))
                    .filter(context -> context.getExpiresAt().isAfter(now));
        }
        String contextCookie = rawContext;
        CsrfContext context;
        if (existing.isPresent()) {
            context = existing.get();
        } else {
            contextCookie = TokenHash.generate(secureRandom);
            context = contextRepository.save(new CsrfContext(
                    TokenHash.sha256(contextCookie),
                    now,
                    now.plus(lifetime)));
        }

        String rawToken = TokenHash.generate(secureRandom);
        LocalDateTime tokenExpiresAt = min(now.plus(lifetime), context.getExpiresAt());
        tokenRepository.save(new CsrfToken(
                context.getContextId(),
                TokenHash.sha256(rawToken),
                now,
                tokenExpiresAt));
        Instant instantExpiry = tokenExpiresAt.toInstant(ZoneOffset.UTC);
        long contextMaxAge = Math.max(0, Duration.between(now, context.getExpiresAt()).getSeconds());
        return new CsrfGrant(contextCookie, rawToken, instantExpiry, contextMaxAge);
    }

    @Transactional(readOnly = true)
    public void requireValid(String rawContext, String rawToken, String origin) {
        if (!isAllowedOrigin(origin) || rawContext == null || rawContext.isBlank()
                || rawToken == null || rawToken.isBlank()) {
            throw invalidCsrf();
        }
        LocalDateTime now = nowUtc();
        CsrfContext context = contextRepository.findByContextHash(TokenHash.sha256(rawContext))
                .filter(candidate -> candidate.getExpiresAt().isAfter(now))
                .orElseThrow(CsrfService::invalidCsrf);
        tokenRepository.findByTokenHash(TokenHash.sha256(rawToken))
                .filter(candidate -> candidate.getContextId().equals(context.getContextId()))
                .filter(candidate -> candidate.getRevokedAt() == null)
                .filter(candidate -> candidate.getExpiresAt().isAfter(now))
                .orElseThrow(CsrfService::invalidCsrf);
    }

    @Transactional
    public void rotate(String rawContext) {
        if (rawContext == null || rawContext.isBlank()) {
            return;
        }
        contextRepository.findByContextHashForUpdate(TokenHash.sha256(rawContext))
                .ifPresent(context -> revokeTokens(context.getContextId(), nowUtc()));
    }

    @Transactional
    public void invalidate(String rawContext) {
        if (rawContext == null || rawContext.isBlank()) {
            return;
        }
        contextRepository.findByContextHashForUpdate(TokenHash.sha256(rawContext))
                .ifPresent(context -> {
                    LocalDateTime now = nowUtc();
                    revokeTokens(context.getContextId(), now);
                    context.expire(now);
                });
    }

    public boolean isAllowedOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return false;
        }
        try {
            URI parsed = URI.create(origin);
            if (parsed.getScheme() == null || parsed.getHost() == null
                    || parsed.getRawPath() != null && !parsed.getRawPath().isEmpty()
                    || parsed.getRawQuery() != null || parsed.getRawFragment() != null) {
                return false;
            }
            return allowedOrigins.contains(parsed.toString());
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void revokeTokens(String contextId, LocalDateTime now) {
        tokenRepository.findAllByContextIdAndRevokedAtIsNull(contextId)
                .forEach(token -> token.revoke(now));
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private static LocalDateTime min(LocalDateTime first, LocalDateTime second) {
        return first.isBefore(second) ? first : second;
    }

    private static ApiException invalidCsrf() {
        return new ApiException(HttpStatus.FORBIDDEN, "CSRF_INVALID",
                "El contexto, token CSRF u origen no es válido. Solicita un token nuevo.");
    }
}
