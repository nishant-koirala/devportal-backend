package com.fonepay.devportal.modules.auth.service;

public interface TempTokenService {

    /** Create a short-lived token for the OTP step: userId + sessionId. */
    String generateTempToken(String userId, String sessionId);

    /** Read userId from the token, or null if it is invalid. */
    String extractUserId(String tempToken);

    /** Read sessionId from the token, or null if it is invalid. */
    String extractSessionId(String tempToken);

    /** True if the token is well-formed and not expired. */
    boolean validateTempToken(String tempToken);

    /** Token lifetime in milliseconds. */
    long getExpirationMs();
}
