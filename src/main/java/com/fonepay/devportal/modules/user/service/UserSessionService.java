package com.fonepay.devportal.modules.user.service;

import java.util.Collection;
import java.util.Optional;

import com.fonepay.devportal.modules.user.document.UserSession;

public interface UserSessionService {

    UserSession createSession(String userId, String ipAddress, String userAgent, Collection<String> roleNames);

    void revokeSessionBySessionId(String sessionId);

    void revokeAllActiveSessions(String userId);

    void revokeAllActiveSessionsExcept(String userId, String currentSessionId);

    Optional<UserSession> getActiveSession(String sessionId);
}
