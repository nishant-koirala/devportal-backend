package com.fonepay.devportal.modules.cms.service.impl;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.dto.response.PublicPageResponseDto;
import com.fonepay.devportal.modules.cms.enums.ProductStatus;
import com.fonepay.devportal.modules.cms.mapper.PublicContentMapper;
import com.fonepay.devportal.modules.cms.repository.PageRepository;
import com.fonepay.devportal.modules.cms.repository.ProductRepository;
import com.fonepay.devportal.modules.cms.service.PublicPageService;

import java.util.Collections;
import com.fonepay.devportal.modules.user.repository.UserProductRepository;
import com.fonepay.devportal.modules.cms.service.PageViewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicPageServiceImpl implements PublicPageService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,100}$");

    private final ProductRepository productRepository;
    private final PageRepository pageRepository;
    private final PublicContentMapper publicContentMapper;
    private final UserProductRepository userProductRepository;
    private final PageViewService pageViewService;

    @Override
    public PublicPageResponseDto getPublishedPage(String productSlug, String pageSlug, String developerId) {
        validateSlug(productSlug, "Product slug");
        validateSlug(pageSlug, "Page slug");

        // Verify product is published
        Product product = productRepository.findBySlugAndStatus(productSlug.trim().toLowerCase(), ProductStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Published product not found with slug: " + productSlug));

        // Query-level projection: Strictly exclude draft_blocks
        Page page = pageRepository.findPublishedByProductIdAndSlugExcludingDrafts(product.getId(), pageSlug.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Published page not found with slug '" + pageSlug + "' under product '" + productSlug + "'"));

        PublicPageResponseDto response = publicContentMapper.toPublicPageResponseDto(page);

        // Zero-Trust Payload Stripping
        if (developerId == null || !userProductRepository.existsByUserIdAndProductId(developerId, product.getId())) {
            response.setPublishedBlocks(Collections.emptyList());
            response.setAddPrompt(true);
        } else {
            // Sequence Diagram D3 Integration: Record page view for subscribed developer
            pageViewService.recordView(developerId, page.getId());
        }

        return response;
    }

    private void validateSlug(String slug, String fieldName) {
        if (slug == null || slug.isBlank()) {
            throw new BadRequestException(fieldName + " must not be blank");
        }
        if (!SLUG_PATTERN.matcher(slug.trim()).matches()) {
            throw new BadRequestException("Invalid format for " + fieldName + ". Must be 1-100 alphanumeric characters, hyphens or underscores.");
        }
    }
}
