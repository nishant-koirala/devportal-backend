package com.fonepay.devportal.modules.developer.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.DuplicateResourceException;
import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.developer.document.UserBookmark;
import com.fonepay.devportal.modules.developer.dto.request.CreateBookmarkRequest;
import com.fonepay.devportal.modules.developer.dto.response.UserBookmarkResponse;
import com.fonepay.devportal.modules.developer.mapper.UserBookmarkMapper;
import com.fonepay.devportal.modules.developer.repository.UserBookmarkRepository;
import com.fonepay.devportal.modules.developer.service.UserBookmarkService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBookmarkServiceImpl implements UserBookmarkService {

    private final UserBookmarkRepository userBookmarkRepository;
    private final UserBookmarkMapper userBookmarkMapper;
    private final Clock clock;

    @Override
    public UserBookmarkResponse createBookmark(String userId, CreateBookmarkRequest request) {
        String pageId = trimToNull(request.getPageId());
        String pageUrl = trimToNull(request.getPageUrl());
        String title = trimToNull(request.getTitle());

        log.info("Creating bookmark for user [{}] on pageId=[{}], pageUrl=[{}]", userId, pageId, pageUrl);

        // Pre-check for duplicate bookmark by pageId
        if (pageId != null && userBookmarkRepository.existsByUserIdAndPageId(userId, pageId)) {
            log.warn("User [{}] already bookmarked pageId [{}]", userId, pageId);
            throw new DuplicateResourceException("Bookmark already exists for this page");
        }

        // Pre-check for duplicate bookmark by pageUrl
        if (pageUrl != null && userBookmarkRepository.existsByUserIdAndPageUrl(userId, pageUrl)) {
            log.warn("User [{}] already bookmarked pageUrl [{}]", userId, pageUrl);
            throw new DuplicateResourceException("Bookmark already exists for this page");
        }

        Instant now = Instant.now(clock);
        UserBookmark bookmark = UserBookmark.builder()
                .id(IdGenerator.nextUlid())
                .userId(userId)
                .pageId(pageId)
                .pageUrl(pageUrl)
                .title(title)
                .createdAt(now)
                .build();

        try {
            UserBookmark saved = userBookmarkRepository.save(bookmark);
            log.info("Bookmark created with ID [{}] for user [{}]", saved.getId(), userId);
            return userBookmarkMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate key caught when saving bookmark for user [{}]: {}", userId, e.getMessage());
            throw new DuplicateResourceException("Bookmark already exists for this page");
        }
    }

    @Override
    public List<UserBookmarkResponse> getUserBookmarks(String userId) {
        log.info("Fetching bookmarks for user [{}]", userId);
        List<UserBookmark> bookmarks = userBookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return userBookmarkMapper.toResponseList(bookmarks);
    }

    @Override
    public void deleteBookmark(String userId, String bookmarkId) {
        log.info("User [{}] attempting to delete bookmark [{}]", userId, bookmarkId);

        UserBookmark bookmark = userBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found with ID: " + bookmarkId));

        if (!userId.equals(bookmark.getUserId())) {
            log.warn("User [{}] unauthorized to delete bookmark [{}] owned by [{}]", userId, bookmarkId, bookmark.getUserId());
            throw new ForbiddenException("You do not have permission to delete this bookmark");
        }

        userBookmarkRepository.delete(bookmark);
        log.info("Bookmark [{}] successfully deleted by user [{}]", bookmarkId, userId);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public void deleteBookmarksByPageId(String pageId) {
        log.info("Cascade deleting bookmarks for deleted page [{}]", pageId);
        userBookmarkRepository.deleteByPageId(pageId);
    }
}
