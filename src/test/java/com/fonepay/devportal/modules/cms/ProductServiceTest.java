package com.fonepay.devportal.modules.cms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.DuplicateResourceException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.document.ProductResource;
import com.fonepay.devportal.modules.cms.dto.request.CreateProductRequest;
import com.fonepay.devportal.modules.cms.dto.request.CreateProductResourceRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductResourceRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdateProductStatusRequest;
import com.fonepay.devportal.modules.cms.dto.response.ProductDetailResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.ProductResourceResponseDto;
import com.fonepay.devportal.modules.cms.enums.ProductStatus;
import com.fonepay.devportal.modules.cms.mapper.ProductMapper;
import com.fonepay.devportal.modules.cms.repository.ProductRepository;
import com.fonepay.devportal.modules.cms.service.AuditLogService;
import com.fonepay.devportal.modules.cms.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private AuditLogService auditLogService;

    private ProductMapper productMapper;
    private Clock clock;
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productMapper = Mappers.getMapper(ProductMapper.class);
        clock = Clock.fixed(Instant.parse("2026-08-25T10:00:00Z"), ZoneId.of("UTC"));
        productService = new ProductServiceImpl(productRepository, mongoTemplate, productMapper, auditLogService, clock);
    }

    @Test
    @DisplayName("Create product - success")
    void createProduct_success() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Fonepay QR")
                .slug("fonepay-qr")
                .shortDescription("QR Payment solution")
                .description("Detailed description of QR Payment solution")
                .status(ProductStatus.ACTIVE)
                .displayOrder(1)
                .resources(List.of(
                        CreateProductResourceRequest.builder()
                                .name("API Docs")
                                .resourceType("SWAGGER")
                                .url("https://api.fonepay.com/docs")
                                .displayOrder(1)
                                .isActive(true)
                                .build()
                ))
                .build();

        when(productRepository.existsBySlug("fonepay-qr")).thenReturn(false);
        when(productRepository.existsByName("Fonepay QR")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDetailResponseDto result = productService.createProduct(request, "admin_123", "192.168.1.10");

        assertNotNull(result);
        assertEquals("Fonepay QR", result.getName());
        assertEquals("fonepay-qr", result.getSlug());
        assertEquals(ProductStatus.ACTIVE, result.getStatus());
        assertEquals(1, result.getResources().size());
        assertEquals("API Docs", result.getResources().get(0).getName());

        verify(auditLogService, times(1)).logAction(
                eq("admin_123"),
                eq("CREATE_PRODUCT"),
                anyString(),
                eq("PRODUCT"),
                eq("192.168.1.10")
        );
    }

    @Test
    @DisplayName("Create product - duplicate slug throws DuplicateResourceException")
    void createProduct_duplicateSlug() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Fonepay QR")
                .slug("fonepay-qr")
                .build();

        when(productRepository.existsBySlug("fonepay-qr")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                productService.createProduct(request, "admin_123", "192.168.1.10")
        );

        verify(productRepository, never()).save(any());
        verify(auditLogService, never()).logAction(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Create product - invalid slug format throws BadRequestException")
    void createProduct_invalidSlug() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Fonepay QR")
                .slug("fonepay QR with spaces!")
                .build();

        assertThrows(BadRequestException.class, () ->
                productService.createProduct(request, "admin_123", "192.168.1.10")
        );
    }

    @Test
    @DisplayName("Get product by ID - success and not found")
    void getProductById_scenarios() {
        Product product = Product.builder()
                .id("prod_01")
                .name("Fonepay Direct")
                .slug("fonepay-direct")
                .status(ProductStatus.ACTIVE)
                .build();

        when(productRepository.findById("prod_01")).thenReturn(Optional.of(product));
        when(productRepository.findById("unknown")).thenReturn(Optional.empty());

        ProductDetailResponseDto found = productService.getProductById("prod_01");
        assertNotNull(found);
        assertEquals("Fonepay Direct", found.getName());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById("unknown"));
    }

    @Test
    @DisplayName("Update product - optimistic locking conflict")
    void updateProduct_optimisticLockConflict() {
        Product existing = Product.builder()
                .id("prod_01")
                .name("Fonepay Direct")
                .slug("fonepay-direct")
                .status(ProductStatus.DRAFT)
                .version(1L)
                .build();

        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Fonepay Direct Updated")
                .slug("fonepay-direct")
                .version(1L)
                .build();

        when(productRepository.findById("prod_01")).thenReturn(Optional.of(existing));
        when(productRepository.existsBySlugAndIdNot("fonepay-direct", "prod_01")).thenReturn(false);
        when(productRepository.existsByNameAndIdNot("Fonepay Direct Updated", "prod_01")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenThrow(new OptimisticLockingFailureException("Version mismatch"));

        assertThrows(DuplicateResourceException.class, () ->
                productService.updateProduct("prod_01", request, "admin_123", "192.168.1.10")
        );
    }

    @Test
    @DisplayName("Update product status - success and audit log")
    void updateProductStatus_success() {
        Product existing = Product.builder()
                .id("prod_01")
                .name("Fonepay Direct")
                .slug("fonepay-direct")
                .status(ProductStatus.DRAFT)
                .build();

        when(productRepository.findById("prod_01")).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        UpdateProductStatusRequest request = UpdateProductStatusRequest.builder()
                .status(ProductStatus.ACTIVE)
                .build();

        ProductDetailResponseDto result = productService.updateProductStatus("prod_01", request, "admin_123", "192.168.1.10");

        assertEquals(ProductStatus.ACTIVE, result.getStatus());
        verify(auditLogService, times(1)).logAction(
                eq("admin_123"),
                eq("UPDATE_PRODUCT_STATUS"),
                eq("prod_01"),
                eq("PRODUCT"),
                eq("192.168.1.10")
        );
    }

    @Test
    @DisplayName("Delete product - success and audit log")
    void deleteProduct_success() {
        Product existing = Product.builder()
                .id("prod_01")
                .name("Fonepay Direct")
                .slug("fonepay-direct")
                .status(ProductStatus.DEPRECATED)
                .build();

        when(productRepository.findById("prod_01")).thenReturn(Optional.of(existing));

        productService.deleteProduct("prod_01", "admin_123", "192.168.1.10");

        verify(productRepository, times(1)).delete(existing);
        verify(auditLogService, times(1)).logAction(
                eq("admin_123"),
                eq("DELETE_PRODUCT"),
                eq("prod_01"),
                eq("PRODUCT"),
                eq("192.168.1.10")
        );
    }

    @Test
    @DisplayName("Embedded ProductResource - Add, Update, Delete operations")
    void embeddedResource_fullLifecycle() {
        Product product = Product.builder()
                .id("prod_01")
                .name("Fonepay API")
                .slug("fonepay-api")
                .status(ProductStatus.ACTIVE)
                .resources(new ArrayList<>())
                .build();

        when(productRepository.findById("prod_01")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        // 1. Add Resource
        CreateProductResourceRequest addReq = CreateProductResourceRequest.builder()
                .name("Postman Collection")
                .resourceType("POSTMAN")
                .url("https://postman.com/fonepay")
                .displayOrder(1)
                .isActive(true)
                .build();

        ProductDetailResponseDto addResult = productService.addResource("prod_01", addReq, "admin_123", "127.0.0.1");
        assertEquals(1, addResult.getResources().size());
        String resId = addResult.getResources().get(0).getResourceId();
        assertNotNull(resId);
        verify(auditLogService, times(1)).logAction(eq("admin_123"), eq("ADD_PRODUCT_RESOURCE"), eq(resId), eq("PRODUCT_RESOURCE"), eq("127.0.0.1"));

        // 2. Update Resource
        UpdateProductResourceRequest updateReq = UpdateProductResourceRequest.builder()
                .name("Updated Postman Collection")
                .resourceType("POSTMAN")
                .url("https://postman.com/fonepay/v2")
                .displayOrder(2)
                .isActive(false)
                .build();

        ProductDetailResponseDto updateResult = productService.updateResource("prod_01", resId, updateReq, "admin_123", "127.0.0.1");
        ProductResourceResponseDto updatedRes = updateResult.getResources().get(0);
        assertEquals("Updated Postman Collection", updatedRes.getName());
        assertEquals("https://postman.com/fonepay/v2", updatedRes.getUrl());
        assertFalse(updatedRes.isActive());
        verify(auditLogService, times(1)).logAction(eq("admin_123"), eq("UPDATE_PRODUCT_RESOURCE"), eq(resId), eq("PRODUCT_RESOURCE"), eq("127.0.0.1"));

        // 3. Get Resources
        List<ProductResourceResponseDto> resourcesList = productService.getResources("prod_01");
        assertEquals(1, resourcesList.size());

        // 4. Delete Resource
        ProductDetailResponseDto deleteResult = productService.deleteResource("prod_01", resId, "admin_123", "127.0.0.1");
        assertTrue(deleteResult.getResources().isEmpty());
        verify(auditLogService, times(1)).logAction(eq("admin_123"), eq("DELETE_PRODUCT_RESOURCE"), eq(resId), eq("PRODUCT_RESOURCE"), eq("127.0.0.1"));
    }
}
