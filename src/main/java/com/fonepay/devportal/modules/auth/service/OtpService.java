package com.fonepay.devportal.modules.auth.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.OtpStatus;
import com.fonepay.devportal.modules.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${app.otp.max-attempts:3}")
    private int maxAttempts;

    /**
     * Generate a new 6-digit OTP code and set it on the user.
     *
     * @param user the user to generate OTP for
     * @return the generated OTP code
     */
    public String generateOtp(User user) {
        // Generate 6-digit numeric OTP
        int otpNumber = secureRandom.nextInt(900000) + 100000; // 100000 to 999999
        String otpCode = String.valueOf(otpNumber);

        Instant now = clock.instant();
        Instant expiresAt = now.plusSeconds(otpExpirationMinutes * 60L);

        user.setOtpCode(otpCode);
        user.setOtpExpiresAt(expiresAt);
        user.setOtpAttempts(0);
        user.setOtpStatus(OtpStatus.PENDING);
        user.setOtpVerifiedAt(null);

        log.info("Generated OTP for user: {} (expires at: {})", user.getUserId(), expiresAt);
        return otpCode;
    }

    /**
     * Verify the provided OTP code against the user's stored OTP.
     *
     * @param user the user to verify OTP for
     * @param providedCode the OTP code provided by the user
     * @return true if OTP is valid and verified, false otherwise
     */
    public boolean verifyOtp(User user, String providedCode) {
        if (user.getOtpStatus() == OtpStatus.VERIFIED) {
            log.warn("OTP already verified for user: {}", user.getUserId());
            return true;
        }

        if (user.getOtpStatus() == OtpStatus.EXPIRED || user.getOtpStatus() == OtpStatus.FAILED) {
            log.warn("OTP in terminal state for user: {}", user.getUserId());
            return false;
        }

        // Check if OTP exists
        if (user.getOtpCode() == null) {
            log.warn("No OTP found for user: {}", user.getUserId());
            return false;
        }

        // Check expiry
        Instant now = clock.instant();
        if (user.getOtpExpiresAt() != null && user.getOtpExpiresAt().isBefore(now)) {
            user.setOtpStatus(OtpStatus.EXPIRED);
            log.warn("OTP expired for user: {}", user.getUserId());
            return false;
        }

        // Check max attempts
        if (user.getOtpAttempts() >= maxAttempts) {
            user.setOtpStatus(OtpStatus.FAILED);
            log.warn("Max OTP attempts exceeded for user: {}", user.getUserId());
            return false;
        }

        // Increment attempts
        user.setOtpAttempts(user.getOtpAttempts() + 1);

        // Verify code
        if (user.getOtpCode().equals(providedCode)) {
            user.setOtpStatus(OtpStatus.VERIFIED);
            user.setOtpVerifiedAt(now);
            log.info("OTP verified successfully for user: {}", user.getUserId());
            return true;
        }

        log.warn("Invalid OTP provided for user: {} (attempt {}/{})", user.getUserId(), user.getOtpAttempts(), maxAttempts);
        return false;
    }

    /**
     * Check if user has a valid OTP in progress.
     *
     * @param user the user to check
     * @return true if OTP is pending and not expired
     */
    public boolean hasPendingOtp(User user) {
        if (user.getOtpStatus() != OtpStatus.PENDING) {
            return false;
        }
        if (user.getOtpExpiresAt() == null) {
            return false;
        }
        return user.getOtpExpiresAt().isAfter(clock.instant());
    }

    /**
     * Clear OTP data from user (e.g., after successful login or when regenerating).
     *
     * @param user the user to clear OTP for
     */
    public void clearOtp(User user) {
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        user.setOtpAttempts(0);
        user.setOtpStatus(OtpStatus.NONE);
        user.setOtpVerifiedAt(null);
    }

    /**
     * Get remaining time in seconds before OTP expires.
     *
     * @param user the user to check
     * @return seconds remaining, or 0 if expired/no OTP
     */
    public long getOtpRemainingSeconds(User user) {
        if (user.getOtpExpiresAt() == null) {
            return 0;
        }
        long remaining = java.time.Duration.between(clock.instant(), user.getOtpExpiresAt()).getSeconds();
        return Math.max(0, remaining);
    }
}