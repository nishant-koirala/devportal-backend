package com.fonepay.devportal.modules.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.PendingAuthStatus;
import com.fonepay.devportal.modules.auth.document.PendingAuth;
import com.fonepay.devportal.modules.auth.repository.PendingAuthRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PendingAuthCleanupService {

    private final PendingAuthRepository pendingAuthRepository;
    private final Clock clock;

    /**
     * Clean up expired pending auth records.
     * Runs every minute to expire and delete stale records.
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void cleanupExpiredPendingAuths() {
        Instant now = Instant.now(clock);
        List<PendingAuth> expired = pendingAuthRepository.findByExpiresAtBeforeAndStatus(now, PendingAuthStatus.PENDING);

        if (!expired.isEmpty()) {
            pendingAuthRepository.deleteAll(expired);
            log.info("Cleaned up {} expired pending auth record(s)", expired.size());
        }
    }
}