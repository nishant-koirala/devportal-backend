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

    @Override
    public PublicPageResponseDto getPublishedPage(String productSlug, String pageSlug) {
        validateSlug(productSlug, "Product slug");
        validateSlug(pageSlug, "Page slug");

        // Verify product is published
        Product product = productRepository.findBySlugAndStatus(productSlug.trim().toLowerCase(), ProductStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Published product not found with slug: " + productSlug));

        // Query-level projection: Strictly exclude draft_blocks
        Page page = pageRepository.findPublishedByProductIdAndSlugExcludingDrafts(product.getId(), pageSlug.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Published page not found with slug '" + pageSlug + "' under product '" + productSlug + "'"));

        // Map to PublicPageResponseDto which contains publishedBlocks only
        return publicContentMapper.toPublicPageResponseDto(page);
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
