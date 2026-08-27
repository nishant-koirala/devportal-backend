package com.fonepay.devportal.modules.cms.service;

import java.util.List;

import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.dto.request.PublishPageRequest;
import com.fonepay.devportal.modules.cms.dto.response.PageMetaResponse;
import com.fonepay.devportal.modules.cms.dto.response.PageVersionResponse;

public interface PublishService {

    PageMetaResponse publishPage(String pageId, PublishPageRequest request, String adminId, String sourceIp);

    PageMetaResponse publishPage(Page page, PublishPageRequest request, String adminId, String sourceIp);

    List<PageMetaResponse> publishPagesForProduct(String productId, String adminId, String sourceIp, String commitMessage);

    List<PageVersionResponse> getPageVersions(String pageId);

    PageVersionResponse getPageVersion(String pageId, int versionNumber);

    PageMetaResponse revertToVersion(String pageId, int versionNumber, String adminId, String sourceIp);
}

