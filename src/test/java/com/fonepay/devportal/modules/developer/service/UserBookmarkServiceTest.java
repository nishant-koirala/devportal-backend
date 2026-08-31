package com.fonepay.devportal.modules.developer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import com.fonepay.devportal.common.exception.DuplicateResourceException;
import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.developer.document.UserBookmark;
import com.fonepay.devportal.modules.developer.dto.request.CreateBookmarkRequest;
import com.fonepay.devportal.modules.developer.dto.response.UserBookmarkResponse;
import com.fonepay.devportal.modules.developer.mapper.UserBookmarkMapper;
import com.fonepay.devportal.modules.developer.repository.UserBookmarkRepository;
import com.fonepay.devportal.modules.developer.service.impl.UserBookmarkServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserBookmarkServiceTest {

    @Mock
    private UserBookmarkRepository userBookmarkRepository;

    private UserBookmarkMapper userBookmarkMapper;
    private Clock fixedClock;
    private UserBookmarkService userBookmarkService;

    private final Instant now = Instant.parse("2026-08-31T12:00:00Z");

    @BeforeEach
    void setUp() {
        userBookmarkMapper = new UserBookmarkMapper();
        fixedClock = Clock.fixed(now, ZoneId.of("UTC"));
        userBookmarkService = new UserBookmarkServiceImpl(userBookmarkRepository, userBookmarkMapper, fixedClock);
    }

    @Test
    @DisplayName("Create bookmark succeeds with valid request")
    void createBookmark_Success() {
        String userId = "user-123";
        CreateBookmarkRequest request = CreateBookmarkRequest.builder()
                .pageId("page-001")
                .pageUrl("/docs/quickstart")
                .title("Quickstart Guide")
                .build();

        when(userBookmarkRepository.existsByUserIdAndPageId(userId, "page-001")).thenReturn(false);
        when(userBookmarkRepository.existsByUserIdAndPageUrl(userId, "/docs/quickstart")).thenReturn(false);
        when(userBookmarkRepository.save(any(UserBookmark.class))).thenAnswer(invocation -> {
            UserBookmark b = invocation.getArgument(0);
            return b;
        });

        UserBookmarkResponse response = userBookmarkService.createBookmark(userId, request);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("page-001", response.getPageId());
        assertEquals("/docs/quickstart", response.getPageUrl());
        assertEquals("Quickstart Guide", response.getTitle());
        assertEquals(now, response.getCreatedAt());
        verify(userBookmarkRepository).save(any(UserBookmark.class));
    }

    @Test
    @DisplayName("Duplicate bookmark by pageId is rejected with DuplicateResourceException")
    void createBookmark_DuplicatePageId_ThrowsException() {
        String userId = "user-123";
        CreateBookmarkRequest request = CreateBookmarkRequest.builder()
                .pageId("page-001")
                .build();

        when(userBookmarkRepository.existsByUserIdAndPageId(userId, "page-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                userBookmarkService.createBookmark(userId, request)
        );
        verify(userBookmarkRepository, never()).save(any());
    }

    @Test
    @DisplayName("Duplicate bookmark by pageUrl is rejected with DuplicateResourceException")
    void createBookmark_DuplicatePageUrl_ThrowsException() {
        String userId = "user-123";
        CreateBookmarkRequest request = CreateBookmarkRequest.builder()
                .pageUrl("/docs/payments")
                .build();

        when(userBookmarkRepository.existsByUserIdAndPageUrl(userId, "/docs/payments")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                userBookmarkService.createBookmark(userId, request)
        );
        verify(userBookmarkRepository, never()).save(any());
    }

    @Test
    @DisplayName("DuplicateKeyException from Mongo unique index is mapped to DuplicateResourceException")
    void createBookmark_MongoDuplicateKey_ThrowsDuplicateResourceException() {
        String userId = "user-123";
        CreateBookmarkRequest request = CreateBookmarkRequest.builder()
                .pageId("page-001")
                .build();

        when(userBookmarkRepository.existsByUserIdAndPageId(userId, "page-001")).thenReturn(false);
        when(userBookmarkRepository.save(any(UserBookmark.class))).thenThrow(new DuplicateKeyException("Duplicate key index"));

        assertThrows(DuplicateResourceException.class, () ->
                userBookmarkService.createBookmark(userId, request)
        );
    }

    @Test
    @DisplayName("List returns only the authenticated user's bookmarks")
    void getUserBookmarks_ReturnsOnlyUserBookmarks() {
        String userId = "user-123";
        UserBookmark b1 = UserBookmark.builder()
                .id("bm-1")
                .userId(userId)
                .pageId("page-001")
                .pageUrl("/docs/one")
                .title("Doc 1")
                .createdAt(now)
                .build();
        UserBookmark b2 = UserBookmark.builder()
                .id("bm-2")
                .userId(userId)
                .pageId("page-002")
                .pageUrl("/docs/two")
                .title("Doc 2")
                .createdAt(now.minusSeconds(60))
                .build();

        when(userBookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(b1, b2));

        List<UserBookmarkResponse> results = userBookmarkService.getUserBookmarks(userId);

        assertEquals(2, results.size());
        assertEquals("bm-1", results.get(0).getId());
        assertEquals("bm-2", results.get(1).getId());
        verify(userBookmarkRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("Delete own bookmark succeeds")
    void deleteBookmark_OwnBookmark_Success() {
        String userId = "user-123";
        String bookmarkId = "bm-1";
        UserBookmark bookmark = UserBookmark.builder()
                .id(bookmarkId)
                .userId(userId)
                .pageId("page-001")
                .build();

        when(userBookmarkRepository.findById(bookmarkId)).thenReturn(Optional.of(bookmark));

        userBookmarkService.deleteBookmark(userId, bookmarkId);

        verify(userBookmarkRepository).delete(bookmark);
    }

    @Test
    @DisplayName("Delete non-existent bookmark throws ResourceNotFoundException (404)")
    void deleteBookmark_NotFound_ThrowsResourceNotFoundException() {
        String userId = "user-123";
        String bookmarkId = "non-existent";

        when(userBookmarkRepository.findById(bookmarkId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userBookmarkService.deleteBookmark(userId, bookmarkId)
        );
        verify(userBookmarkRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete another user's bookmark throws ForbiddenException (403)")
    void deleteBookmark_AnotherUsersBookmark_ThrowsForbiddenException() {
        String currentUserId = "user-123";
        String otherUserId = "user-999";
        String bookmarkId = "bm-foreign";
        UserBookmark foreignBookmark = UserBookmark.builder()
                .id(bookmarkId)
                .userId(otherUserId)
                .pageId("page-001")
                .build();

        when(userBookmarkRepository.findById(bookmarkId)).thenReturn(Optional.of(foreignBookmark));

        assertThrows(ForbiddenException.class, () ->
                userBookmarkService.deleteBookmark(currentUserId, bookmarkId)
        );
        verify(userBookmarkRepository, never()).delete(any());
    }
}
