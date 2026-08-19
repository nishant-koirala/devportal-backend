package com.fonepay.devportal.modules.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.exception.InvalidOrExpiredTokenException;
import com.fonepay.devportal.common.exception.TooManyRequestsException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.repository.UserTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
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
            throw new InvalidOrExpiredTokenException("Token has already been used");
        }

        if (token.getExpiresAt().isBefore(Instant.now(clock))) {
            throw new InvalidOrExpiredTokenException("Token has expired");
        }

        token.setUsedAt(Instant.now(clock));
        return tokenRepository.save(token);
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
}
