package com.fonepay.devportal.modules.developer.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fonepay.devportal.modules.developer.document.UserBookmark;
import com.fonepay.devportal.modules.developer.dto.response.UserBookmarkResponse;

@Component
public class UserBookmarkMapper {

    public UserBookmarkResponse toResponse(UserBookmark bookmark) {
        if (bookmark == null) {
            return null;
        }

        return UserBookmarkResponse.builder()
                .id(bookmark.getId())
                .userId(bookmark.getUserId())
                .pageId(bookmark.getPageId())
                .pageUrl(bookmark.getPageUrl())
                .title(bookmark.getTitle())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }

    public List<UserBookmarkResponse> toResponseList(List<UserBookmark> bookmarks) {
        if (bookmarks == null || bookmarks.isEmpty()) {
            return Collections.emptyList();
        }

        return bookmarks.stream()
                .map(this::toResponse)
                .toList();
    }
}
