package com.fonepay.devportal.modules.user.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCleanupService {

    private final UserRepository userRepository;
    private final Clock clock;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupStalePendingUsers() {
        log.info("Starting cleanup of stale unverified users...");
        Instant cutoff = Instant.now(clock).minus(24, ChronoUnit.HOURS);
        
        // TODO (follow-up, not blocking): also delete UserToken rows for the userIds being removed, since MongoDB has no cascade delete.
        userRepository.deleteByEmailVerifiedFalseAndCreatedAtBefore(cutoff);
        log.info("Finished cleanup of stale unverified users.");
    }
}
