package com.fonepay.devportal.modules.cms.service;

import java.util.List;

import com.fonepay.devportal.modules.cms.dto.request.CreatePageRequest;
import com.fonepay.devportal.modules.cms.dto.request.PageHierarchyUpdateDto;
import com.fonepay.devportal.modules.cms.dto.request.RejectPageRequest;
import com.fonepay.devportal.modules.cms.dto.request.UpdatePageRequest;
import com.fonepay.devportal.modules.cms.dto.response.PageMetaResponse;
import com.fonepay.devportal.modules.cms.dto.response.PageTreeNodeResponse;
import com.fonepay.devportal.security.annotation.RequireAdmin;

public interface PageService {

    PageMetaResponse createPage(String productId, CreatePageRequest request, String createdBy);

    PageMetaResponse getPage(String pageId);

    PageMetaResponse updatePage(String pageId, UpdatePageRequest request);

    PageMetaResponse archivePage(String pageId);

    PageMetaResponse submitForReview(String pageId, String userId, String sourceIp);

    @RequireAdmin
    PageMetaResponse approvePage(String pageId, String adminId, String sourceIp);

    @RequireAdmin
    PageMetaResponse rejectPage(String pageId, RejectPageRequest request, String adminId, String sourceIp);

    List<PageTreeNodeResponse> getPageTree(String productId);

    void movePages(String productId, List<PageHierarchyUpdateDto> updates);
}
