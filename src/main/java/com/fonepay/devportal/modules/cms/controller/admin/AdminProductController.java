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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.common.util.HttpRequestUtil;
import com.fonepay.devportal.modules.cms.dto.request.CreateProductRequest;
import com.fonepay.devportal.modules.cms.dto.request.CreateProductResourceRequest;
import com.fonepay.devportal.modules.cms.dto.request.ProductSearchCriteriaDto;
import com.fonepay.devportal.modules.cms.dto.request.RejectProductRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductResourceRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductStatusRequest;
import com.fonepay.devportal.modules.cms.dto.response.ProductDetailResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.ProductResourceResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.ProductResponseDto;
import com.fonepay.devportal.modules.cms.service.ProductService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.security.annotation.RequireAdmin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping(ApiRoutes.Admin.PRODUCTS)
@RequireAdmin
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final Clock clock;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponseDto>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Admin [{}] creating new product: {}", adminId, request.getName());

        ProductDetailResponseDto response = productService.createProduct(request, adminId, sourceIp);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProductDetailResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Product created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponseDto>>> getProducts(
            @Valid @ModelAttribute ProductSearchCriteriaDto criteria) {

        log.info("Admin fetching products list with criteria: {}", criteria);
        PageResponse<ProductResponseDto> response = productService.getProducts(criteria);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<ProductResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Products retrieved successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping(ApiRoutes.Admin.PRODUCT_BY_ID)
    public ResponseEntity<ApiResponse<ProductDetailResponseDto>> getProductById(@PathVariable String id) {
        log.info("Admin fetching product details for ID: {}", id);
        ProductDetailResponseDto response = productService.getProductById(id);

        return ResponseEntity.ok(
                ApiResponse.<ProductDetailResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product details retrieved successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PutMapping(ApiRoutes.Admin.PRODUCT_BY_ID)
    public ResponseEntity<ApiResponse<ProductDetailResponseDto>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Admin [{}] updating product ID: {}", adminId, id);

        ProductDetailResponseDto response = productService.updateProduct(id, request, adminId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<ProductDetailResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PatchMapping(ApiRoutes.Admin.PRODUCT_STATUS)
    public ResponseEntity<ApiResponse<ProductDetailResponseDto>> updateProductStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductStatusRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Admin [{}] updating product status for ID: {} to {}", adminId, id, request.getStatus());

        ProductDetailResponseDto response = productService.updateProductStatus(id, request, adminId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<ProductDetailResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product status updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Admin.PRODUCT_SUBMIT_REVIEW)
    public ResponseEntity<ApiResponse<ProductDetailResponseDto>> submitForReview(
            @PathVariable String id,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String userId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("User [{}] submitting product ID: {} for review", userId, id);

        ProductDetailResponseDto response = productService.submitForReview(id, userId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<ProductDetailResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product submitted for review successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Admin.PRODUCT_APPROVE)
    public ResponseEntity<ApiResponse<ProductDetailResponseDto>> approveProduct(
            @PathVariable String id,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Admin [{}] approving product ID: {}", adminId, id);

        ProductDetailResponseDto response = productService.approveProduct(id, adminId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<ProductDetailResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product approved and published successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Admin.PRODUCT_REJECT)
    public ResponseEntity<ApiResponse<ProductDetailResponseDto>> rejectProduct(
            @PathVariable String id,
            @Valid @RequestBody RejectProductRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Admin [{}] rejecting product ID: {} with reason: {}", adminId, id, request.getReason());

        ProductDetailResponseDto response = productService.rejectProduct(id, request, adminId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<ProductDetailResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product rejected and returned to draft with feedback")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @DeleteMapping(ApiRoutes.Admin.PRODUCT_BY_ID)
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable String id,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Admin [{}] deleting product ID: {}", adminId, id);

        productService.deleteProduct(id, adminId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Product deleted successfully")
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    // Embedded ProductResource endpoints

    @PostMapping(ApiRoutes.Admin.PRODUCT_RESOURCES)
    public ResponseEntity<ApiResponse<ProductDetailResponseDto>> addResource(
            @PathVariable String id,
            @Valid @RequestBody CreateProductResourceRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Admin [{}] adding resource to product ID: {}", adminId, id);

        ProductDetailResponseDto response = productService.addResource(id, request, adminId, sourceIp);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ProductDetailResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Resource added to product successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PutMapping(ApiRoutes.Admin.PRODUCT_RESOURCE_BY_ID)
    public ResponseEntity<ApiResponse<ProductDetailResponseDto>> updateResource(
            @PathVariable String id,
            @PathVariable String resourceId,
            @Valid @RequestBody UpdateProductResourceRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Admin [{}] updating resource ID: {} in product ID: {}", adminId, resourceId, id);

        ProductDetailResponseDto response = productService.updateResource(id, resourceId, request, adminId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<ProductDetailResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Resource updated successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @DeleteMapping(ApiRoutes.Admin.PRODUCT_RESOURCE_BY_ID)
    public ResponseEntity<ApiResponse<ProductDetailResponseDto>> deleteResource(
            @PathVariable String id,
            @PathVariable String resourceId,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String adminId = extractAdminId(authentication);
        String sourceIp = HttpRequestUtil.getClientIp(httpRequest);
        log.info("Admin [{}] deleting resource ID: {} from product ID: {}", adminId, resourceId, id);

        ProductDetailResponseDto response = productService.deleteResource(id, resourceId, adminId, sourceIp);

        return ResponseEntity.ok(
                ApiResponse.<ProductDetailResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Resource deleted successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping(ApiRoutes.Admin.PRODUCT_RESOURCES)
    public ResponseEntity<ApiResponse<List<ProductResourceResponseDto>>> getResources(@PathVariable String id) {
        log.info("Admin fetching resources for product ID: {}", id);
        List<ProductResourceResponseDto> response = productService.getResources(id);

        return ResponseEntity.ok(
                ApiResponse.<List<ProductResourceResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Resources retrieved successfully")
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
