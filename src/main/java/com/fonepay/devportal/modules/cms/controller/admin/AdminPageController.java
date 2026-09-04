package com.fonepay.devportal.modules.cms.controller.admin;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import org.springframework.security.access.prepost.PreAuthorize;
import com.fonepay.devportal.security.Permissions;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.util.HttpRequestUtil;
import com.fonepay.devportal.modules.cms.document.Block;
import com.fonepay.devportal.modules.cms.dto.request.BlockCreateRequest;
import com.fonepay.devportal.modules.cms.dto.request.BlockReorderRequest;
import com.fonepay.devportal.modules.cms.dto.request.BlockUpdateRequest;
import com.fonepay.devportal.modules.cms.dto.request.CreatePageRequest;
import com.fonepay.devportal.modules.cms.dto.request.BulkPageSaveRequest;
import com.fonepay.devportal.modules.cms.dto.request.PublishPageRequest;
import com.fonepay.devportal.modules.cms.dto.request.RejectPageRequest;
import com.fonepay.devportal.modules.cms.dto.request.ReorderPagesRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdatePageRequest;
import com.fonepay.devportal.modules.cms.dto.response.PageMetaResponse;
import com.fonepay.devportal.modules.cms.dto.response.PageTreeNodeResponse;
import com.fonepay.devportal.modules.cms.dto.response.PageVersionResponse;
import com.fonepay.devportal.modules.cms.service.BlockService;
import com.fonepay.devportal.modules.cms.service.PageService;
import com.fonepay.devportal.modules.cms.service.PublishService;
import com.fonepay.devportal.modules.cms.service.RevisionService;
import com.fonepay.devportal.modules.cms.document.Revision;
import com.fonepay.devportal.modules.user.document.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Cms.BASE)
@PreAuthorize("hasAuthority('" + Permissions.CMS_PAGE_EDIT + "')")
@RequiredArgsConstructor
public class AdminPageController {

    private final PageService pageService;
    private final BlockService blockService;
    private final PublishService publishService;
    private final RevisionService revisionService;
    private final Clock clock;

    @PostMapping(ApiRoutes.Cms.PRODUCT_PAGES)
    public ResponseEntity<ApiResponse<PageMetaResponse>> createPage(
            @PathVariable @NotBlank String productId,
            @Valid @RequestBody CreatePageRequest request,
            Authentication authentication) {

        String adminId = extractAdminId(authentication);
        log.info("Creating page '{}' for product {}", request.getSlug(), productId);
        PageMetaResponse response = pageService.createPage(productId, request, adminId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<PageMetaResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Page created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping(ApiRoutes.Cms.PRODUCT_PAGE_TREE)
    public ResponseEntity<ApiResponse<List<PageTreeNodeResponse>>> getPageTree(
            @PathVariable @NotBlank String productId) {

        List<PageTreeNodeResponse> tree = pageService.getPageTree(productId);

        return ResponseEntity.ok(
                ApiResponse.<List<PageTreeNodeResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page tree retrieved successfully")
                        .data(tree)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PutMapping(ApiRoutes.Cms.PRODUCT_PAGES_REORDER)
    public ResponseEntity<ApiResponse<Void>> reorderPages(
            @PathVariable @NotBlank String productId,
            @Valid @RequestBody ReorderPagesRequest request) {

        log.info("Reordering {} page(s) for product {}", request.getUpdates().size(), productId);
        pageService.movePages(productId, request.getUpdates());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page hierarchy updated successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping(ApiRoutes.Cms.PAGE_BY_ID)
    public ResponseEntity<ApiResponse<PageMetaResponse>> getPage(@PathVariable @NotBlank String pageId) {
        PageMetaResponse response = pageService.getPage(pageId);

        return ResponseEntity.ok(
                ApiResponse.<PageMetaResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page retrieved successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PatchMapping(ApiRoutes.Cms.PAGE_BY_ID)
    public ResponseEntity<ApiResponse<PageMetaResponse>> updatePage(
            @PathVariable @NotBlank String pageId,
            @Valid @RequestBody UpdatePageRequest request) {

        PageMetaResponse response = pageService.updatePage(pageId, request);

        return ResponseEntity.ok(
                ApiResponse.<PageMetaResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PutMapping(ApiRoutes.Cms.PAGE_BY_ID)
    public ResponseEntity<ApiResponse<PageMetaResponse>> bulkSavePage(
            @PathVariable @NotBlank String pageId,
            @Valid @RequestBody BulkPageSaveRequest request,
            Authentication authentication) {

        String adminId = extractAdminId(authentication);
        PageMetaResponse response = pageService.bulkSavePage(pageId, request, adminId);

        return ResponseEntity.ok(
                ApiResponse.<PageMetaResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page saved successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @DeleteMapping(ApiRoutes.Cms.PAGE_BY_ID)
    public ResponseEntity<ApiResponse<PageMetaResponse>> archivePage(@PathVariable @NotBlank String pageId) {
        log.info("Archiving page {}", pageId);
        PageMetaResponse response = pageService.archivePage(pageId);

        return ResponseEntity.ok(
                ApiResponse.<PageMetaResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page archived successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Cms.PAGE_SUBMIT_REVIEW)
    public ResponseEntity<ApiResponse<PageMetaResponse>> submitForReview(
            @PathVariable @NotBlank String pageId,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String userId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("User [{}] submitting page {} for review", userId, pageId);

        PageMetaResponse response = pageService.submitForReview(pageId, userId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<PageMetaResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page submitted for review successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PreAuthorize("hasAuthority('" + Permissions.CMS_PAGE_APPROVE + "')")
    @PostMapping(ApiRoutes.Cms.PAGE_APPROVE)
    public ResponseEntity<ApiResponse<PageMetaResponse>> approvePage(
            @PathVariable @NotBlank String pageId,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Admin [{}] approving page {}", adminId, pageId);

        PageMetaResponse response = pageService.approvePage(pageId, adminId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<PageMetaResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page approved and published successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PreAuthorize("hasAuthority('" + Permissions.CMS_PAGE_APPROVE + "')")
    @PostMapping(ApiRoutes.Cms.PAGE_REJECT)
    public ResponseEntity<ApiResponse<PageMetaResponse>> rejectPage(
            @PathVariable @NotBlank String pageId,
            @Valid @RequestBody RejectPageRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Admin [{}] rejecting page {} with reason: {}", adminId, pageId, request.getReason());

        PageMetaResponse response = pageService.rejectPage(pageId, request, adminId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<PageMetaResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page rejected and returned to draft with feedback")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @Deprecated
    @PostMapping(ApiRoutes.Cms.PAGE_BY_ID + "/blocks")
    public ResponseEntity<ApiResponse<Block>> addBlock(
            @PathVariable String pageId,
            @Valid @RequestBody BlockCreateRequest request) {

        Block block = blockService.addBlock(pageId, request.getType(), request.getData(), request.getOrder());

        return ResponseEntity.ok(
                ApiResponse.<Block>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Block added successfully")
                        .data(block)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @Deprecated
    @PutMapping(ApiRoutes.Cms.PAGE_BY_ID + "/blocks/{blockId}")
    public ResponseEntity<ApiResponse<Void>> updateBlock(
            @PathVariable String pageId,
            @PathVariable String blockId,
            @Valid @RequestBody BlockUpdateRequest request) {

        blockService.updateBlockData(pageId, blockId, request.getData(), request.getCurrentVersion());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Block updated successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @Deprecated
    @PatchMapping(ApiRoutes.Cms.PAGE_BY_ID + "/blocks/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderBlocks(
            @PathVariable String pageId,
            @Valid @RequestBody BlockReorderRequest request) {

        blockService.reorderBlocks(pageId, request.getBlockIds());

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Blocks reordered successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @Deprecated
    @DeleteMapping(ApiRoutes.Cms.PAGE_BY_ID + "/blocks/{blockId}")
    public ResponseEntity<ApiResponse<Void>> deleteBlock(
            @PathVariable String pageId,
            @PathVariable String blockId) {

        blockService.deleteBlock(pageId, blockId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Block deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    // Publish and Versioning Controller
    @PreAuthorize("hasAuthority('" + Permissions.CMS_PAGE_PUBLISH + "')")
    @PostMapping(ApiRoutes.Cms.PAGE_PUBLISH)
    public ResponseEntity<ApiResponse<PageMetaResponse>> publishPage(
            @PathVariable @NotBlank String pageId,
            @Valid @RequestBody PublishPageRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Publishing page {} by admin {}", pageId, adminId);

        PageMetaResponse response = publishService.publishPage(pageId, request, adminId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<PageMetaResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page published successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping(ApiRoutes.Cms.PAGE_VERSIONS)
    public ResponseEntity<ApiResponse<List<PageVersionResponse>>> getPageVersions(
            @PathVariable @NotBlank String pageId) {

        List<PageVersionResponse> versions = publishService.getPageVersions(pageId);

        return ResponseEntity.ok(
                ApiResponse.<List<PageVersionResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page versions retrieved successfully")
                        .data(versions)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping(ApiRoutes.Cms.PAGE_VERSION_BY_NUMBER)
    public ResponseEntity<ApiResponse<PageVersionResponse>> getPageVersion(
            @PathVariable @NotBlank String pageId,
            @PathVariable int versionNumber) {

        PageVersionResponse version = publishService.getPageVersion(pageId, versionNumber);

        return ResponseEntity.ok(
                ApiResponse.<PageVersionResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page version retrieved successfully")
                        .data(version)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Cms.PAGE_REVERT)
    public ResponseEntity<ApiResponse<PageMetaResponse>> revertPageVersion(
            @PathVariable @NotBlank String pageId,
            @PathVariable int versionNumber,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Reverting page {} to version {} by admin {}", pageId, versionNumber, adminId);

        PageMetaResponse response = publishService.revertToVersion(pageId, versionNumber, adminId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<PageMetaResponse>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page reverted to version " + versionNumber + " successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Cms.PAGE_BY_ID + "/revisions/{version}/restore")
    public ResponseEntity<ApiResponse<Revision>> restoreRevision(
            @PathVariable @NotBlank String pageId,
            @PathVariable int version,
            Authentication authentication) {

        String adminId = extractAdminId(authentication);
        log.info("Restoring page {} to revision {} by admin {}", pageId, version, adminId);

        Revision response = revisionService.revertToVersion(pageId, version, adminId);

        return ResponseEntity.ok(
                ApiResponse.<Revision>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Page draft restored to revision " + version + " successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    private String extractAdminId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user.getUserId();
            }
            if (principal instanceof String str && !"anonymousUser".equalsIgnoreCase(str)) {
                return str;
            }
            if (authentication.getName() != null && !"anonymousUser".equalsIgnoreCase(authentication.getName())) {
                return authentication.getName();
            }
        }
        return "UNKNOWN_ADMIN";
    }
}
