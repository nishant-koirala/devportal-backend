package com.fonepay.devportal.modules.cms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.cms.document.Block;
import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.document.ParagraphBlockData;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.document.ProductResource;
import com.fonepay.devportal.modules.cms.dto.response.PublicPageResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.PublicProductResponseDto;
import com.fonepay.devportal.modules.cms.enums.BlockType;
import com.fonepay.devportal.modules.cms.enums.PageStatus;
import com.fonepay.devportal.modules.cms.enums.ProductStatus;
import com.fonepay.devportal.modules.cms.mapper.PublicContentMapper;
import com.fonepay.devportal.modules.cms.repository.PageRepository;
import com.fonepay.devportal.modules.cms.repository.ProductRepository;
import com.fonepay.devportal.modules.cms.service.impl.PublicPageServiceImpl;
import com.fonepay.devportal.modules.cms.service.impl.PublicProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class PublicContentServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PageRepository pageRepository;

    private PublicContentMapper publicContentMapper;
    private PublicProductServiceImpl publicProductService;
    private PublicPageServiceImpl publicPageService;

    @BeforeEach
    void setUp() {
        publicContentMapper = Mappers.getMapper(PublicContentMapper.class);
        publicProductService = new PublicProductServiceImpl(productRepository, publicContentMapper);
        publicPageService = new PublicPageServiceImpl(productRepository, pageRepository, publicContentMapper);
    }

    @Test
    @DisplayName("Public Products - lists active products and filters out inactive resources")
    void getActiveProducts_filtersInactiveResources() {
        Product prod1 = Product.builder()
                .id("p1")
                .name("Fonepay QR")
                .slug("fonepay-qr")
                .status(ProductStatus.ACTIVE)
                .displayOrder(1)
                .resources(List.of(
                        ProductResource.builder().resourceId("r1").name("Public Doc").url("https://api.fonepay.com/doc").isActive(true).build(),
                        ProductResource.builder().resourceId("r2").name("Internal Staging").url("https://staging.internal").isActive(false).build()
                ))
                .build();

        when(productRepository.findByStatusOrderByDisplayOrderAsc(ProductStatus.ACTIVE)).thenReturn(List.of(prod1));

        List<PublicProductResponseDto> results = publicProductService.getActiveProducts();

        assertEquals(1, results.size());
        assertEquals("fonepay-qr", results.get(0).getSlug());
        // Only active resources should be present in public response
        assertEquals(1, results.get(0).getResources().size());
        assertEquals("Public Doc", results.get(0).getResources().get(0).getName());
    }

    @Test
    @DisplayName("Public Product by Slug - returns active product; throws 404 for draft/deprecated")
    void getActiveProductBySlug_successAndNotFound() {
        Product prod = Product.builder()
                .id("p1")
                .name("Fonepay QR")
                .slug("fonepay-qr")
                .status(ProductStatus.ACTIVE)
                .build();

        when(productRepository.findBySlugAndStatus("fonepay-qr", ProductStatus.ACTIVE)).thenReturn(Optional.of(prod));
        when(productRepository.findBySlugAndStatus("draft-product", ProductStatus.ACTIVE)).thenReturn(Optional.empty());

        PublicProductResponseDto found = publicProductService.getActiveProductBySlug("fonepay-qr");
        assertNotNull(found);
        assertEquals("fonepay-qr", found.getSlug());

        assertThrows(ResourceNotFoundException.class, () -> publicProductService.getActiveProductBySlug("draft-product"));
    }

    @Test
    @DisplayName("Public Page - fetches published page and returns only publishedBlocks")
    void getPublishedPage_success() {
        Product product = Product.builder()
                .id("p_123")
                .slug("fonepay-qr")
                .status(ProductStatus.ACTIVE)
                .build();

        Block publishedBlock = new Block("b1", BlockType.PARAGRAPH, 1, 1L, new ParagraphBlockData("Welcome to QR API"));
        Block draftBlock = new Block("b2", BlockType.PARAGRAPH, 2, 2L, new ParagraphBlockData("Secret draft content"));

        Page page = new Page();
        page.setId("page_01");
        page.setProductId("p_123");
        page.setTitle("Getting Started");
        page.setSlug("getting-started");
        page.setStatus(PageStatus.PUBLISHED);
        page.setPublishedBlocks(List.of(publishedBlock));
        page.setDraftBlocks(List.of(draftBlock));
        page.setLastPublishedAt(Instant.parse("2026-08-25T08:00:00Z"));


        when(productRepository.findBySlugAndStatus("fonepay-qr", ProductStatus.ACTIVE)).thenReturn(Optional.of(product));
        when(pageRepository.findPublishedByProductIdAndSlugExcludingDrafts("p_123", "getting-started")).thenReturn(Optional.of(page));

        PublicPageResponseDto result = publicPageService.getPublishedPage("fonepay-qr", "getting-started");

        assertNotNull(result);
        assertEquals("Getting Started", result.getTitle());
        assertEquals("getting-started", result.getSlug());
        assertEquals(1, result.getPublishedBlocks().size());
        assertEquals("b1", result.getPublishedBlocks().get(0).getId());
    }

    @Test
    @DisplayName("Public Page - invalid slug formats throw BadRequestException")
    void getPublishedPage_invalidSlugs() {
        assertThrows(BadRequestException.class, () -> publicPageService.getPublishedPage("../escape/path", "valid-page"));
        assertThrows(BadRequestException.class, () -> publicPageService.getPublishedPage("valid-product", "../../etc/passwd"));
    }
}
