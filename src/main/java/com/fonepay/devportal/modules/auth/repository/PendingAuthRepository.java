package com.fonepay.devportal.modules.auth.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.common.constant.enums.PendingAuthStatus;
import com.fonepay.devportal.modules.auth.document.PendingAuth;

@Repository
public interface PendingAuthRepository extends MongoRepository<PendingAuth, String> {

    Optional<PendingAuth> findByUserIdAndStatus(String userId, PendingAuthStatus status);

    Optional<PendingAuth> findById(String id);

    List<PendingAuth> findByExpiresAtBeforeAndStatus(Instant expiresAt, PendingAuthStatus status);
}