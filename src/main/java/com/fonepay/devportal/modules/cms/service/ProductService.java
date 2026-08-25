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
import com.fonepay.devportal.security.annotation.RequireAdmin;

public interface ProductService {

    @RequireAdmin
    ProductDetailResponseDto createProduct(CreateProductRequest request, String adminId, String sourceIp);

    ProductDetailResponseDto getProductById(String id);

    ProductDetailResponseDto getProductBySlug(String slug);

    @RequireAdmin
    ProductDetailResponseDto updateProduct(String id, UpdateProductRequest request, String adminId, String sourceIp);

    @RequireAdmin
    ProductDetailResponseDto updateProductStatus(String id, UpdateProductStatusRequest request, String adminId, String sourceIp);

    @RequireAdmin
    void deleteProduct(String id, String adminId, String sourceIp);

    PageResponse<ProductResponseDto> getProducts(ProductSearchCriteriaDto criteria);

    // Embedded ProductResource operations
    @RequireAdmin
    ProductDetailResponseDto addResource(String productId, CreateProductResourceRequest request, String adminId, String sourceIp);

    @RequireAdmin
    ProductDetailResponseDto updateResource(String productId, String resourceId, UpdateProductResourceRequest request, String adminId, String sourceIp);

    @RequireAdmin
    ProductDetailResponseDto deleteResource(String productId, String resourceId, String adminId, String sourceIp);

    List<ProductResourceResponseDto> getResources(String productId);
}
