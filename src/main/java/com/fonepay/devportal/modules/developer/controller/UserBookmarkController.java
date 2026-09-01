package com.fonepay.devportal.modules.developer.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.developer.dto.request.CreateBookmarkRequest;
import com.fonepay.devportal.modules.developer.dto.response.UserBookmarkResponse;
import com.fonepay.devportal.modules.developer.service.UserBookmarkService;
import com.fonepay.devportal.modules.user.document.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Profile.BASE + ApiRoutes.Profile.BOOKMARKS)
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserBookmarkController {

    private final UserBookmarkService userBookmarkService;
    private final Clock clock;

    @PostMapping
    public ResponseEntity<ApiResponse<UserBookmarkResponse>> createBookmark(
            @Valid @RequestBody CreateBookmarkRequest request,
            Authentication authentication) {

        User user = extractUser(authentication);
        log.info("Request to create bookmark received for user [{}]", user.getUserId());
        UserBookmarkResponse response = userBookmarkService.createBookmark(user.getUserId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UserBookmarkResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Bookmark created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserBookmarkResponse>>> getBookmarks(
            Authentication authentication) {

        User user = extractUser(authentication);
        log.info("Request to fetch bookmarks for user [{}]", user.getUserId());
        List<UserBookmarkResponse> responses = userBookmarkService.getUserBookmarks(user.getUserId());

        return ResponseEntity.ok(
                ApiResponse.<List<UserBookmarkResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Bookmarks retrieved successfully")
                        .data(responses)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(
            @PathVariable @NotBlank String bookmarkId,
            Authentication authentication) {

        User user = extractUser(authentication);
        log.info("Request to delete bookmark [{}] for user [{}]", bookmarkId, user.getUserId());
        userBookmarkService.deleteBookmark(user.getUserId(), bookmarkId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Bookmark deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    private User extractUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user;
            }
        }
        throw new UnauthorizedException("User session is invalid or unauthenticated");
    }
}
