package com.fonepay.devportal.modules.cms.service;

import com.fonepay.devportal.modules.cms.dto.response.PublicPageResponseDto;

public interface PublicPageService {

    PublicPageResponseDto getPublishedPage(String productSlug, String pageSlug);
}
