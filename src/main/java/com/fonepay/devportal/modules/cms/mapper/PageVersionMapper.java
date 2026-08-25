package com.fonepay.devportal.modules.cms.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.fonepay.devportal.modules.cms.document.PageVersion;
import com.fonepay.devportal.modules.cms.dto.response.PageVersionResponse;

@Mapper(componentModel = "spring")
public interface PageVersionMapper {

    PageVersionResponse toResponse(PageVersion pageVersion);

    List<PageVersionResponse> toResponseList(List<PageVersion> pageVersions);
}
