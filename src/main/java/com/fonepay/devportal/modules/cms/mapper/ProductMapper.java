package com.fonepay.devportal.modules.cms.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.document.ProductResource;
import com.fonepay.devportal.modules.cms.dto.request.CreateProductResourceRequest;
import com.fonepay.devportal.modules.cms.dto.response.ProductDetailResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.ProductResourceResponseDto;
import com.fonepay.devportal.modules.cms.dto.response.ProductResponseDto;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "resourceCount", source = "resources", qualifiedByName = "calculateResourceCount")
    ProductResponseDto toResponseDto(Product product);

    List<ProductResponseDto> toResponseDtoList(List<Product> products);

    ProductDetailResponseDto toDetailResponseDto(Product product);

    ProductResourceResponseDto toResourceResponseDto(ProductResource resource);

    List<ProductResourceResponseDto> toResourceResponseDtoList(List<ProductResource> resources);

    @Mapping(target = "resourceId", ignore = true)
    ProductResource toResource(CreateProductResourceRequest request);

    @Named("calculateResourceCount")
    default int calculateResourceCount(List<ProductResource> resources) {
        return resources != null ? resources.size() : 0;
    }
}
