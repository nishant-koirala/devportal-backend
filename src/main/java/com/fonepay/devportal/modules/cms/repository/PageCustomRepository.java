package com.fonepay.devportal.modules.cms.repository;

import com.fonepay.devportal.modules.cms.document.BlockData;

public interface PageCustomRepository {
    
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
