package com.fonepay.devportal.modules.cms.service;

import com.fonepay.devportal.modules.cms.document.Block;
import com.fonepay.devportal.modules.cms.document.BlockData;
import com.fonepay.devportal.modules.cms.enums.BlockType;

import java.util.List;

public interface BlockService {

    /**
     * Adds a new block to a page.
     * @param pageId The ID of the page.
     * @param type The type of the block (HEADING, PARAGRAPH, etc.).
     * @param data The content payload of the block.
     * @param order Optional specific order to insert at. If null, append to the end.
     * @return The newly created Block.
     */
    Block addBlock(String pageId, BlockType type, BlockData data, Integer order);

    /**
     * Updates an existing block's data.
     * @param pageId The ID of the page.
     * @param blockId The ID of the block to update.
     * @param data The new block data payload.
     * @param currentVersion The version of the block before the update for optimistic locking.
     * @return true if successful.
     */
    boolean updateBlockData(String pageId, String blockId, BlockData data, long currentVersion);

    /**
     * Reorders the blocks in a page based on a new sequence of IDs.
     * @param pageId The ID of the page.
     * @param blockIds The new ordered list of block IDs.
     */
    void reorderBlocks(String pageId, List<String> blockIds);

    /**
     * Deletes a block from a page.
     * @param pageId The ID of the page.
     * @param blockId The ID of the block to remove.
     */
    void deleteBlock(String pageId, String blockId);
}
