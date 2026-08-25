package com.fonepay.devportal.modules.cms.service.impl;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.dto.response.PublicProductResponseDto;
import com.fonepay.devportal.modules.cms.enums.ProductStatus;
import com.fonepay.devportal.modules.cms.mapper.PublicContentMapper;
import com.fonepay.devportal.modules.cms.repository.ProductRepository;
import com.fonepay.devportal.modules.cms.service.PublicProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicProductServiceImpl implements PublicProductService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{1,100}$");

    private final ProductRepository productRepository;
    private final PublicContentMapper publicContentMapper;

    @Override
    public List<PublicProductResponseDto> getActiveProducts() {
        // Query-level filtering: Fetch only ACTIVE products sorted by displayOrder
        List<Product> activeProducts = productRepository.findByStatusOrderByDisplayOrderAsc(ProductStatus.ACTIVE);
        return publicContentMapper.toPublicProductResponseDtoList(activeProducts);
    }

    @Override
    public PublicProductResponseDto getActiveProductBySlug(String slug) {
        validateSlug(slug);

        // Query-level filtering: Fetch only ACTIVE product matching slug
        Product product = productRepository.findBySlugAndStatus(slug.trim().toLowerCase(), ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active product not found with slug: " + slug));

        return publicContentMapper.toPublicProductResponseDto(product);
    }

    private void validateSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new BadRequestException("Slug must not be blank");
        }
        if (!SLUG_PATTERN.matcher(slug.trim()).matches()) {
            throw new BadRequestException("Invalid slug parameter format");
        }
    }
}
