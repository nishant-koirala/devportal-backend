package com.fonepay.devportal.modules.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TempTokenService {

    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.temp-token-expiration-ms:300000}")
    private long tempTokenExpirationMs;

    /**
     * Generate a temporary token for OTP authentication flow.
     * Format: base64(userId:sessionId:timestamp:randomNonce)
     */
    public String generateTempToken(String userId, String sessionId) {
        long timestamp = clock.millis();
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        String randomNonce = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        String rawToken = String.format("%s:%s:%d:%s", userId, sessionId, timestamp, randomNonce);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extract user ID from temporary token.
     * Returns null if token is invalid or expired.
     */
    public String extractUserId(String tempToken) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(tempToken), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length < 4) {
                return null;
            }
            return parts[0];
        } catch (Exception e) {
            log.error("Failed to extract user ID from temp token", e);
            return null;
        }
    }

    /**
     * Extract session ID from temporary token.
     * Returns null if token is invalid or expired.
     */
    public String extractSessionId(String tempToken) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(tempToken), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length < 4) {
                return null;
            }
            return parts[1];
        } catch (Exception e) {
            log.error("Failed to extract session ID from temp token", e);
            return null;
        }
    }

    /**
     * Validate the temporary token.
     * Checks if it's properly formatted and not expired.
     */
    public boolean validateTempToken(String tempToken) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(tempToken), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length < 4) {
                return false;
            }

            long timestamp = Long.parseLong(parts[2]);
            long now = clock.millis();

            // Check if token is expired
            if (now - timestamp > tempTokenExpirationMs) {
                log.warn("Temp token expired. Token timestamp: {}, Current time: {}", timestamp, now);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to validate temp token", e);
            return false;
        }
    }

    /**
     * Get the expiration time in milliseconds.
     */
    public long getExpirationMs() {
        return tempTokenExpirationMs;
    }
}