package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.modules.auth.document.UserToken;

public interface UserTokenService {

    String createAndSaveToken(String userId, TokenType tokenType, long durationHours);

    UserToken createLoginOtpToken(String userId, String otpHash, int expirationMinutes);

    boolean verifyLoginOtp(UserToken token, String providedCode, int maxAttempts);

    void checkRateLimit(String userId, TokenType tokenType, long minSecondsInterval);

    UserToken validateAndConsumeToken(String rawToken, TokenType expectedType);

    UserToken validateToken(String rawToken, TokenType expectedType);

    void consumeToken(UserToken token);

    String hashToken(String rawToken);

    void deleteToken(UserToken token);

    void deleteAllTokensForUser(String userId);

    void deleteUnusedTokensForUser(String userId, TokenType tokenType);
}

