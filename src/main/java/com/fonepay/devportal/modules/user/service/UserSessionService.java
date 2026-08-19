package com.fonepay.devportal.modules.user.service;

import java.util.Optional;

import com.fonepay.devportal.modules.user.document.UserSession;

public interface UserSessionService {

    UserSession createSession(String userId, String ipAddress, String userAgent, long expirationMs);

    void revokeSessionBySessionId(String sessionId);

    void revokeAllActiveSessions(String userId);

    Optional<UserSession> getActiveSession(String sessionId);
}
