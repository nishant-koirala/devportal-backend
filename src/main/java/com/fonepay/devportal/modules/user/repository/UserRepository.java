package com.fonepay.devportal.modules.user.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.user.document.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    void deleteByEmailVerifiedFalseAndCreatedAtBefore(java.time.Instant cutoff);

    void deleteByEmailVerifiedFalseAndCreatedAtBeforeAndRolesRoleNameNotIn(java.time.Instant cutoff, java.util.List<String> excludedRoles);
}
