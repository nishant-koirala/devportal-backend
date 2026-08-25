package com.fonepay.devportal.modules.cms.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.cms.dto.request.CreatePageRequest;
import com.fonepay.devportal.modules.cms.dto.request.ReorderPagesRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdatePageRequest;
import com.fonepay.devportal.modules.cms.dto.response.PageMetaResponse;
import com.fonepay.devportal.modules.cms.dto.response.PageTreeNodeResponse;
import com.fonepay.devportal.modules.cms.service.PageService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.security.annotation.RequireEditor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Cms.BASE)
@RequireEditor
@RequiredArgsConstructor
public class AdminPageController {

    private final PageService pageService;
    private final Clock clock;

    @PostMapping(ApiRoutes.Cms.PRODUCT_PAGES)
    public ResponseEntity<ApiResponse<PageMetaResponse>> createPage(
            @PathVariable @NotBlank String productId,
            @Valid @RequestBody CreatePageRequest request,
            @AuthenticationPrincipal User user) {

        log.info("Creating page '{}' for product {}", request.getSlug(), productId);
        PageMetaResponse data = pageService.createPage(productId, request, currentUserId(user));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(success(HttpStatus.CREATED, "Page created successfully", data));
    }

    @GetMapping(ApiRoutes.Cms.PRODUCT_PAGE_TREE)
    public ResponseEntity<ApiResponse<List<PageTreeNodeResponse>>> getPageTree(
            @PathVariable @NotBlank String productId) {
        List<PageTreeNodeResponse> tree = pageService.getPageTree(productId);
        return ResponseEntity.ok(success(HttpStatus.OK, "Page tree retrieved successfully", tree));
    }

    @PutMapping(ApiRoutes.Cms.PRODUCT_PAGES_REORDER)
    public ResponseEntity<ApiResponse<Void>> reorderPages(
            @PathVariable @NotBlank String productId,
            @Valid @RequestBody ReorderPagesRequest request) {

        log.info("Reordering {} page(s) for product {}", request.getUpdates().size(), productId);
        pageService.movePages(productId, request.getUpdates());
        return ResponseEntity.ok(success(HttpStatus.OK, "Page hierarchy updated successfully", null));
    }

    @GetMapping(ApiRoutes.Cms.PAGE_BY_ID)
    public ResponseEntity<ApiResponse<PageMetaResponse>> getPage(@PathVariable @NotBlank String pageId) {
        PageMetaResponse data = pageService.getPage(pageId);
        return ResponseEntity.ok(success(HttpStatus.OK, "Page retrieved successfully", data));
    }

    @PatchMapping(ApiRoutes.Cms.PAGE_BY_ID)
    public ResponseEntity<ApiResponse<PageMetaResponse>> updatePage(
            @PathVariable @NotBlank String pageId,
            @Valid @RequestBody UpdatePageRequest request) {

        PageMetaResponse data = pageService.updatePage(pageId, request);
        return ResponseEntity.ok(success(HttpStatus.OK, "Page updated successfully", data));
    }

    @DeleteMapping(ApiRoutes.Cms.PAGE_BY_ID)
    public ResponseEntity<ApiResponse<PageMetaResponse>> archivePage(@PathVariable @NotBlank String pageId) {
        log.info("Archiving page {}", pageId);
        PageMetaResponse data = pageService.archivePage(pageId);
        return ResponseEntity.ok(success(HttpStatus.OK, "Page archived successfully", data));
    }

    private String currentUserId(User user) {
        if (user == null || user.getUserId() == null || user.getUserId().isBlank()) {
            throw new UnauthorizedException("Authentication required");
        }
        return user.getUserId();
    }

    private <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status.value())
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now(clock))
                .build();
    }
}
