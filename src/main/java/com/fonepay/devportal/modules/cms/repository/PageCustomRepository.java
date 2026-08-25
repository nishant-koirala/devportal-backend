package com.fonepay.devportal.modules.cms.repository;

import java.time.Instant;
import java.util.List;

import com.fonepay.devportal.modules.cms.document.BlockData;
import com.fonepay.devportal.modules.cms.dto.request.PageHierarchyUpdateDto;
import com.fonepay.devportal.modules.cms.enums.PageStatus;

public interface PageCustomRepository {

    void bulkUpdateHierarchy(List<PageHierarchyUpdateDto> updates, Instant updatedAt);

    void bulkSetStatus(List<String> pageIds, PageStatus status, Instant updatedAt);

    /**
     * Updates a specific draft block inside a Page using optimistic locking.
     *
     * @param pageId The ID of the page.
     * @param blockId The ID of the block to update.
     * @param newData The new block data payload.
     * @param currentVersion The version of the block before the update.
     * @return true if the block was updated successfully, false if the version mismatched (concurrent update) or block not found.
     */
    boolean updateDraftBlock(String pageId, String blockId, BlockData newData, long currentVersion);
}
