package com.fonepay.devportal.modules.developer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.developer.document.UserBookmark;

@Repository
public interface UserBookmarkRepository extends MongoRepository<UserBookmark, String> {

    List<UserBookmark> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<UserBookmark> findByUserIdAndPageId(String userId, String pageId);

    Optional<UserBookmark> findByUserIdAndPageUrl(String userId, String pageUrl);

    Optional<UserBookmark> findByIdAndUserId(String id, String userId);

    boolean existsByUserIdAndPageId(String userId, String pageId);

    boolean existsByUserIdAndPageUrl(String userId, String pageUrl);

    void deleteByIdAndUserId(String id, String userId);
}
