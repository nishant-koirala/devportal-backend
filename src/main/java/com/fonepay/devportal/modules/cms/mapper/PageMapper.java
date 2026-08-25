package com.fonepay.devportal.modules.cms.mapper;

import org.mapstruct.Mapper;

import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.dto.response.PageMetaResponse;

@Mapper(componentModel = "spring")
public interface PageMapper {

    PageMetaResponse toMetaResponse(Page page);
}
