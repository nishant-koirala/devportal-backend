package com.fonepay.devportal.modules.developer.service;

import java.util.List;

import com.fonepay.devportal.modules.developer.dto.request.CreateBookmarkRequest;
import com.fonepay.devportal.modules.developer.dto.response.UserBookmarkResponse;

public interface UserBookmarkService {

    UserBookmarkResponse createBookmark(String userId, CreateBookmarkRequest request);

    List<UserBookmarkResponse> getUserBookmarks(String userId);

    void deleteBookmark(String userId, String bookmarkId);

    void deleteBookmarksByPageId(String pageId);
}
