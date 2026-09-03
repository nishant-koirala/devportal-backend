package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.exception.InvalidOrExpiredTokenException;
import com.fonepay.devportal.common.exception.TooManyRequestsException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.repository.UserTokenRepository;
import com.fonepay.devportal.modules.auth.service.UserTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserTokenServiceImpl implements UserTokenService {

    private final UserTokenRepository tokenRepository;
    private final Clock clock;

    @Override
    public String createAndSaveToken(String userId, TokenType tokenType, long durationHours) {
        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hashToken(rawToken);
        Instant now = Instant.now(clock);

        UserToken token = UserToken.builder()
                .id(IdGenerator.nextUlid())
                .userId(userId)
                .tokenHash(hashedToken)
                .tokenType(tokenType)
                .createdAt(now)
                .expiresAt(now.plus(durationHours, ChronoUnit.HOURS))
                .build();

        tokenRepository.save(token);
        log.info("Created {} token for userId: {}", tokenType, userId);
        return rawToken;
    }

    @Override
    public UserToken createLoginOtpToken(String userId, String otpHash, int expirationMinutes) {
        // Clean up any existing login OTP tokens for this user
        tokenRepository.deleteByUserIdAndTokenType(userId, TokenType.LOGIN_OTP);

        Instant now = Instant.now(clock);
        UserToken token = UserToken.builder()
                .id(IdGenerator.nextUlid())
                .userId(userId)
                .tokenHash(otpHash)
                .tokenType(TokenType.LOGIN_OTP)
                .createdAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .attempts(0)
                .build();

        UserToken saved = tokenRepository.save(token);
        log.info("Created LOGIN_OTP token {} for userId: {} (expires at: {})", saved.getId(), userId, saved.getExpiresAt());
        return saved;
    }

    @Override
    public boolean verifyLoginOtp(UserToken token, String providedCode, int maxAttempts) {
        if (token.getUsedAt() != null) {
            log.warn("Login OTP token already used: {}", token.getId());
            return false;
        }

        Instant now = Instant.now(clock);
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(now)) {
            log.warn("Login OTP token expired: {}", token.getId());
            return false;
        }

        if (token.getAttempts() >= maxAttempts) {
            log.warn("Max OTP attempts exceeded for token: {}", token.getId());
            return false;
        }

        token.setAttempts(token.getAttempts() + 1);

        String providedHash = hashToken(providedCode);
        if (token.getTokenHash().equals(providedHash)) {
            token.setUsedAt(now);
            tokenRepository.save(token);
            log.info("Login OTP verified successfully for token: {}", token.getId());
            return true;
        }

        tokenRepository.save(token);
        return false;
    }

    @Override
    public void deleteToken(UserToken token) {
        if (token != null) {
            tokenRepository.delete(token);
        }
    }


    @Override
    public void checkRateLimit(String userId, TokenType tokenType, long minSecondsInterval) {
        Optional<UserToken> existingTokenOpt = tokenRepository
                .findByUserIdAndTokenTypeAndUsedAtIsNull(userId, tokenType);

        if (existingTokenOpt.isPresent()) {
            UserToken existingToken = existingTokenOpt.get();
            long secondsSinceCreation = ChronoUnit.SECONDS.between(existingToken.getCreatedAt(), Instant.now(clock));
            if (secondsSinceCreation < minSecondsInterval) {
                throw new TooManyRequestsException("Please wait " + minSecondsInterval + " seconds before requesting a new token");
            }
            // Delete old unused token before creating a new one
            tokenRepository.delete(existingToken);
        }
    }

    @Override
    public UserToken validateAndConsumeToken(String rawToken, TokenType expectedType) {
        UserToken token = validateToken(rawToken, expectedType);
        consumeToken(token);
        return token;
    }

    @Override
    public UserToken validateToken(String rawToken, TokenType expectedType) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidOrExpiredTokenException("Invalid token");
        }

        String hashedToken = hashToken(rawToken.trim());

        UserToken token = tokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid or expired token"));

        if (token.getTokenType() != expectedType) {
            throw new InvalidOrExpiredTokenException("Invalid token type");
        }

        if (token.getUsedAt() != null) {
            throw new InvalidOrExpiredTokenException("This verification link has already been used.");
        }

        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(Instant.now(clock))) {
            throw new InvalidOrExpiredTokenException("Token has expired");
        }

        return token;
    }

    @Override
    public void consumeToken(UserToken token) {
        token.setUsedAt(Instant.now(clock));
        tokenRepository.save(token);
    }

    @Override
    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize SHA-256 algorithm for token hashing", e);
        }
    }

    @Override
    public void deleteAllTokensForUser(String userId) {
        tokenRepository.deleteByUserId(userId);
        log.info("Deleted all tokens for userId: {}", userId);
    }
}
