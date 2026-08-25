package com.fonepay.devportal.modules.cms.controller.public_portal;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.modules.cms.dto.response.PublicProductResponseDto;
import com.fonepay.devportal.modules.cms.service.PublicProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(ApiRoutes.Public.PRODUCTS)
@RequiredArgsConstructor
public class PublicProductController {

    private final PublicProductService publicProductService;
    private final Clock clock;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PublicProductResponseDto>>> getActiveProducts() {
        log.info("Public request: Fetching active products list");
        List<PublicProductResponseDto> products = publicProductService.getActiveProducts();

        return ResponseEntity.ok(
                ApiResponse.<List<PublicProductResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Active products retrieved successfully")
                        .data(products)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping(ApiRoutes.Public.PRODUCT_BY_SLUG)
    public ResponseEntity<ApiResponse<PublicProductResponseDto>> getActiveProductBySlug(
            @PathVariable String slug) {

        log.info("Public request: Fetching active product details for slug: {}", slug);
        PublicProductResponseDto product = publicProductService.getActiveProductBySlug(slug);

        return ResponseEntity.ok(
                ApiResponse.<PublicProductResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Active product retrieved successfully")
                        .data(product)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }
}
