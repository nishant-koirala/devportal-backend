package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.PendingAuthStatus;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.auth.document.PendingAuth;
import com.fonepay.devportal.modules.auth.repository.PendingAuthRepository;
import com.fonepay.devportal.modules.auth.service.OtpService;
import com.fonepay.devportal.modules.auth.service.PendingAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PendingAuthServiceImpl implements PendingAuthService {

    private final PendingAuthRepository pendingAuthRepository;
    private final OtpService otpService;
    private final Clock clock;

    @Override
    public PendingAuth createPendingAuth(String userId, String otpHash, int expirationMinutes) {
        // Clean up any existing pending auth for this user
        Optional<PendingAuth> existing = pendingAuthRepository.findByUserIdAndStatus(userId, PendingAuthStatus.PENDING);
        existing.ifPresent(pendingAuth -> {
            log.info("Deleting existing pending auth for user: {}", userId);
            pendingAuthRepository.delete(pendingAuth);
        });

        Instant now = Instant.now(clock);
        Instant expiresAt = now.plusSeconds(expirationMinutes * 60L);

        PendingAuth pendingAuth = PendingAuth.builder()
                .id(IdGenerator.nextUlid())
                .userId(userId)
                .otpHash(otpHash)
                .expiresAt(expiresAt)
                .attempts(0)
                .status(PendingAuthStatus.PENDING)
                .createdAt(now)
                .build();

        PendingAuth saved = pendingAuthRepository.save(pendingAuth);
        log.info("Created pending auth {} for user: {} (expires at: {})", saved.getId(), userId, expiresAt);
        return saved;
    }

    @Override
    public Optional<PendingAuth> findById(String id) {
        return pendingAuthRepository.findById(id);
    }

    @Override
    public Optional<PendingAuth> findPendingByUserId(String userId) {
        return pendingAuthRepository.findByUserIdAndStatus(userId, PendingAuthStatus.PENDING);
    }

    @Override
    public boolean verifyOtp(PendingAuth pendingAuth, String providedCode, int maxAttempts) {
        if (pendingAuth.getStatus() == PendingAuthStatus.VERIFIED) {
            log.warn("Pending auth already verified: {}", pendingAuth.getId());
            return true;
        }

        if (pendingAuth.getStatus() == PendingAuthStatus.EXPIRED || pendingAuth.getStatus() == PendingAuthStatus.FAILED) {
            log.warn("Pending auth in terminal state: {}", pendingAuth.getId());
            return false;
        }

        // Check expiry
        Instant now = Instant.now(clock);
        if (pendingAuth.getExpiresAt() != null && pendingAuth.getExpiresAt().isBefore(now)) {
            pendingAuth.setStatus(PendingAuthStatus.EXPIRED);
            pendingAuthRepository.save(pendingAuth);
            log.warn("Pending auth expired: {}", pendingAuth.getId());
            return false;
        }

        // Check max attempts
        if (pendingAuth.getAttempts() >= maxAttempts) {
            pendingAuth.setStatus(PendingAuthStatus.FAILED);
            pendingAuthRepository.save(pendingAuth);
            log.warn("Max OTP attempts exceeded for pending auth: {}", pendingAuth.getId());
            return false;
        }

        // Increment attempts
        pendingAuth.setAttempts(pendingAuth.getAttempts() + 1);

        // Verify code by comparing SHA-256 hash
        String providedHash = otpService.hashOtp(providedCode);
        if (pendingAuth.getOtpHash().equals(providedHash)) {
            pendingAuth.setStatus(PendingAuthStatus.VERIFIED);
            pendingAuth.setVerifiedAt(now);
            pendingAuthRepository.save(pendingAuth);
            log.info("OTP verified successfully for pending auth: {}", pendingAuth.getId());
            return true;
        }

        pendingAuthRepository.save(pendingAuth);
        log.warn("Invalid OTP provided for pending auth: {} (attempt {}/{})", pendingAuth.getId(), pendingAuth.getAttempts(), maxAttempts);
        return false;
    }

    @Override
    public void markVerified(PendingAuth pendingAuth) {
        pendingAuth.setStatus(PendingAuthStatus.VERIFIED);
        pendingAuth.setVerifiedAt(Instant.now(clock));
        pendingAuthRepository.save(pendingAuth);
    }

    @Override
    public void deletePendingAuth(PendingAuth pendingAuth) {
        pendingAuthRepository.delete(pendingAuth);
        log.info("Deleted pending auth: {}", pendingAuth.getId());
    }
}
