package com.fonepay.devportal.modules.cms.repository;

import java.time.Instant;
import java.util.List;

import com.fonepay.devportal.modules.cms.dto.request.PageHierarchyUpdateDto;
import com.fonepay.devportal.modules.cms.enums.PageStatus;

public interface PageCustomRepository {

    void bulkUpdateHierarchy(List<PageHierarchyUpdateDto> updates, Instant updatedAt);

    void bulkSetStatus(List<String> pageIds, PageStatus status, Instant updatedAt);
}
