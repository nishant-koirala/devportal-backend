package com.fonepay.devportal.modules.cms.service;

import java.util.List;

import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.modules.cms.dto.request.CreateProductRequest;
import com.fonepay.devportal.modules.cms.dto.request.CreateProductResourceRequest;
import com.fonepay.devportal.modules.cms.dto.request.ProductSearchCriteriaDto;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductResourceRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductStatusRequest;
import com.fonepay.devportal.modules.cms.dto.response.ProductDetailResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.ProductResourceResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.ProductResponseDto;
import org.springframework.security.access.prepost.PreAuthorize;
import com.fonepay.devportal.security.Permissions;

public interface ProductService {

    @PreAuthorize("hasAuthority('" + Permissions.PRODUCT_MANAGE + "')")
    ProductDetailResponseDto createProduct(CreateProductRequest request, String adminId, String sourceIp);

    ProductDetailResponseDto getProductById(String id);

    ProductDetailResponseDto getProductBySlug(String slug);

    @PreAuthorize("hasAuthority('" + Permissions.PRODUCT_MANAGE + "')")
    ProductDetailResponseDto updateProduct(String id, UpdateProductRequest request, String adminId, String sourceIp);

    @PreAuthorize("hasAuthority('" + Permissions.PRODUCT_MANAGE + "')")
    ProductDetailResponseDto updateProductStatus(String id, UpdateProductStatusRequest request, String adminId, String sourceIp);

    @PreAuthorize("hasAuthority('" + Permissions.PRODUCT_MANAGE + "')")
    ProductDetailResponseDto submitForReview(String id, String userId, String sourceIp);

    @PreAuthorize("hasAuthority('" + Permissions.PRODUCT_MANAGE + "')")
    ProductDetailResponseDto approveProduct(String id, String adminId, String sourceIp);

    @PreAuthorize("hasAuthority('" + Permissions.PRODUCT_MANAGE + "')")
    ProductDetailResponseDto rejectProduct(String id, com.fonepay.devportal.modules.cms.dto.request.RejectProductRequest request, String adminId, String sourceIp);

    @PreAuthorize("hasAuthority('" + Permissions.PRODUCT_MANAGE + "')")
    void deleteProduct(String id, String adminId, String sourceIp);

    PageResponse<ProductResponseDto> getProducts(ProductSearchCriteriaDto criteria);

    // Embedded ProductResource operations
    @PreAuthorize("hasAuthority('" + Permissions.PRODUCT_MANAGE + "')")
    ProductDetailResponseDto addResource(String productId, CreateProductResourceRequest request, String adminId, String sourceIp);

    @PreAuthorize("hasAuthority('" + Permissions.PRODUCT_MANAGE + "')")
    ProductDetailResponseDto updateResource(String productId, String resourceId, UpdateProductResourceRequest request, String adminId, String sourceIp);

    @PreAuthorize("hasAuthority('" + Permissions.PRODUCT_MANAGE + "')")
    ProductDetailResponseDto deleteResource(String productId, String resourceId, String adminId, String sourceIp);

    List<ProductResourceResponseDto> getResources(String productId);
}
