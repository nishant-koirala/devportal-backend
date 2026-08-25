package com.fonepay.devportal.modules.cms.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.document.ProductResource;
import com.fonepay.devportal.modules.cms.dto.response.ProductResourceResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.PublicPageResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.PublicProductResponseDto;

@Mapper(componentModel = "spring")
public interface PublicContentMapper {

    @Mapping(target = "resources", source = "resources", qualifiedByName = "filterActiveResources")
    PublicProductResponseDto toPublicProductResponseDto(Product product);

    List<PublicProductResponseDto> toPublicProductResponseDtoList(List<Product> products);

    @Mapping(target = "publishedBlocks", source = "publishedBlocks")
    PublicPageResponseDto toPublicPageResponseDto(Page page);

    ProductResourceResponseDto toResourceResponseDto(ProductResource resource);

    @Named("filterActiveResources")
    default List<ProductResourceResponseDto> filterActiveResources(List<ProductResource> resources) {
        if (resources == null) {
            return Collections.emptyList();
        }
        return resources.stream()
                .filter(ProductResource::isActive)
                .map(this::toResourceResponseDto)
                .collect(Collectors.toList());
    }
}
