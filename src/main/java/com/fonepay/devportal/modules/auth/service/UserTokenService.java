package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.modules.auth.document.UserToken;

public interface UserTokenService {

    String createAndSaveToken(String userId, TokenType tokenType, long durationHours);

    void checkRateLimit(String userId, TokenType tokenType, long minSecondsInterval);

    UserToken validateAndConsumeToken(String rawToken, TokenType expectedType);

    String hashToken(String rawToken);
}
