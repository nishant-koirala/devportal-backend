package com.fonepay.devportal.modules.developer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fonepay.devportal.common.exception.DuplicateResourceException;
import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.GlobalExceptionHandler;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.developer.dto.request.CreateBookmarkRequest;
import com.fonepay.devportal.modules.developer.dto.response.UserBookmarkResponse;
import com.fonepay.devportal.modules.developer.service.UserBookmarkService;
import com.fonepay.devportal.modules.user.document.User;

@ExtendWith(MockitoExtension.class)
class UserBookmarkControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserBookmarkService userBookmarkService;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-08-31T12:00:00Z"), ZoneId.of("UTC"));

    @InjectMocks
    private UserBookmarkController userBookmarkController;

    private ObjectMapper objectMapper;
    private User testUser;
    private Authentication testAuth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userBookmarkController)
                .setControllerAdvice(new GlobalExceptionHandler(clock))
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUser = new User();
        testUser.setUserId("usr-dev-123");
        testUser.setEmail("dev@fonepay.com");
        testUser.setFullName("Fonepay Developer");

        testAuth = new UsernamePasswordAuthenticationToken(testUser, null, List.of());
    }

    @Test
    @DisplayName("POST /api/v1/profile/bookmarks - Create bookmark succeeds (201 Created)")
    void createBookmark_Success() throws Exception {
        CreateBookmarkRequest request = CreateBookmarkRequest.builder()
                .pageId("pg-101")
                .pageUrl("/docs/api-intro")
                .title("API Intro")
                .build();

        UserBookmarkResponse response = UserBookmarkResponse.builder()
                .id("bm-01")
                .userId(testUser.getUserId())
                .pageId("pg-101")
                .pageUrl("/docs/api-intro")
                .title("API Intro")
                .createdAt(Instant.now(clock))
                .build();

        when(userBookmarkService.createBookmark(eq(testUser.getUserId()), any(CreateBookmarkRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/profile/bookmarks")
                        .principal(testAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("bm-01"))
                .andExpect(jsonPath("$.data.pageId").value("pg-101"))
                .andExpect(jsonPath("$.data.pageUrl").value("/docs/api-intro"))
                .andExpect(jsonPath("$.data.title").value("API Intro"));
    }

    @Test
    @DisplayName("POST /api/v1/profile/bookmarks - Duplicate bookmark rejected (409 Conflict)")
    void createBookmark_Duplicate_ReturnsConflict() throws Exception {
        CreateBookmarkRequest request = CreateBookmarkRequest.builder()
                .pageId("pg-101")
                .build();

        when(userBookmarkService.createBookmark(eq(testUser.getUserId()), any(CreateBookmarkRequest.class)))
                .thenThrow(new DuplicateResourceException("Bookmark already exists for this page"));

        mockMvc.perform(post("/api/v1/profile/bookmarks")
                        .principal(testAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bookmark already exists for this page"));
    }

    @Test
    @DisplayName("POST /api/v1/profile/bookmarks - Invalid request without pageId or pageUrl rejected (400 Bad Request)")
    void createBookmark_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateBookmarkRequest request = CreateBookmarkRequest.builder()
                .title("Only Title No Reference")
                .build();

        mockMvc.perform(post("/api/v1/profile/bookmarks")
                        .principal(testAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/profile/bookmarks - Unauthenticated request rejected (401 Unauthorized)")
    void createBookmark_Unauthenticated_ReturnsUnauthorized() throws Exception {
        CreateBookmarkRequest request = CreateBookmarkRequest.builder()
                .pageId("pg-101")
                .build();

        mockMvc.perform(post("/api/v1/profile/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/profile/bookmarks - List returns only authenticated user's bookmarks (200 OK)")
    void getBookmarks_Success() throws Exception {
        UserBookmarkResponse response = UserBookmarkResponse.builder()
                .id("bm-01")
                .userId(testUser.getUserId())
                .pageId("pg-101")
                .title("API Intro")
                .createdAt(Instant.now(clock))
                .build();

        when(userBookmarkService.getUserBookmarks(testUser.getUserId()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/profile/bookmarks")
                        .principal(testAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("bm-01"))
                .andExpect(jsonPath("$.data[0].userId").value(testUser.getUserId()));
    }

    @Test
    @DisplayName("GET /api/v1/profile/bookmarks - Unauthenticated list request rejected (401 Unauthorized)")
    void getBookmarks_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/profile/bookmarks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("DELETE /api/v1/profile/bookmarks/{bookmarkId} - Delete own bookmark succeeds (200 OK)")
    void deleteBookmark_Success() throws Exception {
        String bookmarkId = "bm-01";
        doNothing().when(userBookmarkService).deleteBookmark(testUser.getUserId(), bookmarkId);

        mockMvc.perform(delete("/api/v1/profile/bookmarks/" + bookmarkId)
                        .principal(testAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bookmark deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/v1/profile/bookmarks/{bookmarkId} - Delete non-existent bookmark returns 404")
    void deleteBookmark_NotFound_ReturnsNotFound() throws Exception {
        String bookmarkId = "non-existent";
        doThrow(new ResourceNotFoundException("Bookmark not found with ID: " + bookmarkId))
                .when(userBookmarkService).deleteBookmark(testUser.getUserId(), bookmarkId);

        mockMvc.perform(delete("/api/v1/profile/bookmarks/" + bookmarkId)
                        .principal(testAuth))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("DELETE /api/v1/profile/bookmarks/{bookmarkId} - Delete another user's bookmark returns 403 Forbidden")
    void deleteBookmark_Forbidden_ReturnsForbidden() throws Exception {
        String bookmarkId = "foreign-bm";
        doThrow(new ForbiddenException("You do not have permission to delete this bookmark"))
                .when(userBookmarkService).deleteBookmark(testUser.getUserId(), bookmarkId);

        mockMvc.perform(delete("/api/v1/profile/bookmarks/" + bookmarkId)
                        .principal(testAuth))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("DELETE /api/v1/profile/bookmarks/{bookmarkId} - Unauthenticated delete request rejected (401 Unauthorized)")
    void deleteBookmark_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/profile/bookmarks/bm-01"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.success").value(false));
    }
}
