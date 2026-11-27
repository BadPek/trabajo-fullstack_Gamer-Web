package com.gamerzone.auth.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.gamerzone.auth.repository.CsrfContextRepository;
import com.gamerzone.auth.repository.CsrfTokenRepository;
import com.gamerzone.auth.repository.RefreshTokenFamilyRepository;
import com.gamerzone.auth.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenFamilyRepository refreshTokenFamilyRepository;
    private final CsrfTokenRepository csrfTokenRepository;
    private final CsrfContextRepository csrfContextRepository;
    private final Clock clock;

    public SessionCleanupService(
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenFamilyRepository refreshTokenFamilyRepository,
            CsrfTokenRepository csrfTokenRepository,
            CsrfContextRepository csrfContextRepository,
            Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenFamilyRepository = refreshTokenFamilyRepository;
        this.csrfTokenRepository = csrfTokenRepository;
        this.csrfContextRepository = csrfContextRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "${auth.session.cleanup-cron:0 0 3 * * *}", zone = "UTC")
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        refreshTokenRepository.deleteForExpiredFamilies(now);
        refreshTokenFamilyRepository.deleteExpired(now);
        csrfTokenRepository.deleteExpired(now);
        csrfContextRepository.deleteExpired(now);
    }
}
