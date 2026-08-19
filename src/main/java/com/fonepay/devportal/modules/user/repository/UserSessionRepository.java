package com.fonepay.devportal.modules.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.common.constant.enums.SessionStatus;
import com.fonepay.devportal.modules.user.document.UserSession;

@Repository
public interface UserSessionRepository extends MongoRepository<UserSession, String> {

    Optional<UserSession> findBySessionIdAndStatus(String sessionId, SessionStatus status);

    List<UserSession> findByUserIdAndStatus(String userId, SessionStatus status);

    List<UserSession> findByUserIdAndIpAddressAndUserAgentAndStatus(String userId, String ipAddress, String userAgent, SessionStatus status);

    Optional<UserSession> findBySessionId(String sessionId);
}
