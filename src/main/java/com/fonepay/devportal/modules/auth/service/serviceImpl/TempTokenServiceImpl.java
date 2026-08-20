package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.modules.auth.service.TempTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TempTokenServiceImpl implements TempTokenService {

    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.temp-token-expiration-ms:300000}")
    private long tempTokenExpirationMs;

    @Override
    public String generateTempToken(String userId, String sessionId) {
        long timestamp = clock.millis();
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        String randomNonce = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        String rawToken = String.format("%s:%s:%d:%s", userId, sessionId, timestamp, randomNonce);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
    }

    @Override
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

    @Override
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

    @Override
    public boolean validateTempToken(String tempToken) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(tempToken), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length < 4) {
                return false;
            }

            long timestamp = Long.parseLong(parts[2]);
            long now = clock.millis();

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

    @Override
    public long getExpirationMs() {
        return tempTokenExpirationMs;
    }
}
