package com.fonepay.devportal.modules.cms.service;

import java.util.List;

import com.fonepay.devportal.modules.cms.dto.request.CreatePageRequest;
import com.fonepay.devportal.modules.cms.dto.request.PageHierarchyUpdateDto;
import com.fonepay.devportal.modules.cms.dto.request.UpdatePageRequest;
import com.fonepay.devportal.modules.cms.dto.response.PageMetaResponse;
import com.fonepay.devportal.modules.cms.dto.response.PageTreeNodeResponse;

public interface PageService {

    PageMetaResponse createPage(String productId, CreatePageRequest request, String createdBy);

    PageMetaResponse getPage(String pageId);

    PageMetaResponse updatePage(String pageId, UpdatePageRequest request);

    PageMetaResponse archivePage(String pageId);

    List<PageTreeNodeResponse> getPageTree(String productId);

    void movePages(String productId, List<PageHierarchyUpdateDto> updates);
}
