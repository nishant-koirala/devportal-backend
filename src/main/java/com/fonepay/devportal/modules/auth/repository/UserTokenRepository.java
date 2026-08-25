package com.fonepay.devportal.modules.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.modules.auth.document.UserToken;

@Repository
public interface UserTokenRepository extends MongoRepository<UserToken, String> {
    
    Optional<UserToken> findByTokenHash(String tokenHash);

    Optional<UserToken> findByIdAndTokenType(String id, TokenType tokenType);

    Optional<UserToken> findByUserIdAndTokenType(String userId, TokenType tokenType);

    Optional<UserToken> findByUserIdAndTokenTypeAndUsedAtIsNull(String userId, TokenType tokenType);

    void deleteByUserId(String userId);

    void deleteByUserIdAndTokenType(String userId, TokenType tokenType);
}

