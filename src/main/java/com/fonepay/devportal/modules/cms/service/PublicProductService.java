package com.fonepay.devportal.modules.cms.service;

import java.util.List;

import com.fonepay.devportal.modules.cms.dto.response.PublicProductResponseDto;

public interface PublicProductService {

    List<PublicProductResponseDto> getActiveProducts();

    PublicProductResponseDto getActiveProductBySlug(String slug);
}
