package com.fonepay.devportal.modules.cms.service.impl;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ConcurrentUpdateException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.cms.document.Block;
import com.fonepay.devportal.modules.cms.document.BlockData;
import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.enums.BlockType;
import com.fonepay.devportal.modules.cms.enums.PageStatus;
import com.fonepay.devportal.modules.cms.repository.PageRepository;
import com.fonepay.devportal.modules.cms.service.BlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    private final PageRepository pageRepository;
    private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    private com.fonepay.devportal.modules.cms.document.BlockData mapToBlockData(BlockType type, java.util.Map<String, Object> data) {
        if (data == null) return null;
        Class<? extends com.fonepay.devportal.modules.cms.document.BlockData> dataClass = switch (type) {
            case HEADING -> com.fonepay.devportal.modules.cms.document.HeadingBlockData.class;
            case PARAGRAPH -> com.fonepay.devportal.modules.cms.document.ParagraphBlockData.class;
            case CODE -> com.fonepay.devportal.modules.cms.document.CodeBlockData.class;
            case ENDPOINT -> com.fonepay.devportal.modules.cms.document.EndpointBlockData.class;
            case FAQ -> com.fonepay.devportal.modules.cms.document.FaqBlockData.class;
            case TABLE -> com.fonepay.devportal.modules.cms.document.TableBlockData.class;
            case IMAGE -> com.fonepay.devportal.modules.cms.document.ImageBlockData.class;
            case NOTE_WARNING -> com.fonepay.devportal.modules.cms.document.NoteWarningBlockData.class;
            case PARAMETER_TABLE -> com.fonepay.devportal.modules.cms.document.ParameterTableBlockData.class;
            case TEST_CREDENTIAL -> com.fonepay.devportal.modules.cms.document.TestCredentialBlockData.class;
        };
        return objectMapper.convertValue(data, dataClass);
    }

    @Override
    public Block addBlock(String pageId, BlockType type, java.util.Map<String, Object> data, Integer specificOrder) {
        Page page = requireEditablePage(pageId);

        List<Block> draftBlocks = page.getDraftBlocks();

        // Calculate order
        int newOrder;
        if (specificOrder != null) {
            newOrder = specificOrder;
            // Shift existing blocks down
            for (Block b : draftBlocks) {
                if (b.getOrder() >= newOrder) {
                    b.setOrder(b.getOrder() + 1);
                }
            }
        } else {
            // Append to end
            newOrder = draftBlocks.stream()
                    .mapToInt(Block::getOrder)
                    .max()
                    .orElse(0) + 1;
        }

        com.fonepay.devportal.modules.cms.document.BlockData parsedData = mapToBlockData(type, data);

        Block newBlock = new Block(
                UUID.randomUUID().toString(),
                type,
                newOrder,
                parsedData
        );

        if (parsedData != null) parsedData.sanitize();

        draftBlocks.add(newBlock);
        
        // Re-sort to maintain array consistency in DB based on order
        draftBlocks.sort(Comparator.comparingInt(Block::getOrder));
        
        pageRepository.save(page);
        
        return newBlock;
    }

    @Override
    public boolean updateBlockData(String pageId, String blockId, java.util.Map<String, Object> data, long currentVersion) {
        Page page = requireEditablePage(pageId);
        
        Block targetBlock = page.getDraftBlocks().stream()
                .filter(b -> b.getId().equals(blockId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Block not found with id: " + blockId));
                
        com.fonepay.devportal.modules.cms.document.BlockData parsedData = mapToBlockData(targetBlock.getType(), data);
        if (parsedData != null) parsedData.sanitize();
        
        boolean success = pageRepository.updateDraftBlock(pageId, blockId, parsedData, currentVersion);
        if (!success) {
            throw new ConcurrentUpdateException("The block has been modified by another user. Please refresh and try again.");
        }
        return true;
    }

    @Override
    public void reorderBlocks(String pageId, List<String> blockIds) {
        Page page = requireEditablePage(pageId);

        List<Block> draftBlocks = page.getDraftBlocks();
        
        // Create a fast lookup map
        Map<String, Block> blockMap = draftBlocks.stream()
                .collect(Collectors.toMap(Block::getId, b -> b));
                
        // Ensure all provided IDs actually exist in the page
        for (String id : blockIds) {
            if (!blockMap.containsKey(id)) {
                throw new ResourceNotFoundException("Block not found in this page with id: " + id);
            }
        }

        // Update the order sequentially based on the list
        for (int i = 0; i < blockIds.size(); i++) {
            Block block = blockMap.get(blockIds.get(i));
            block.setOrder(i + 1);
        }

        // Re-sort the array based on the new orders
        draftBlocks.sort(Comparator.comparingInt(Block::getOrder));

        pageRepository.save(page);
    }

    @Override
    public void deleteBlock(String pageId, String blockId) {
        Page page = requireEditablePage(pageId);

        List<Block> draftBlocks = page.getDraftBlocks();
        
        boolean removed = draftBlocks.removeIf(b -> b.getId().equals(blockId));
        if (!removed) {
            throw new ResourceNotFoundException("Block not found with id: " + blockId);
        }

        // Recalculate order for remaining blocks
        for (int i = 0; i < draftBlocks.size(); i++) {
            draftBlocks.get(i).setOrder(i + 1);
        }

        pageRepository.save(page);
    }

    private Page requireEditablePage(String pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + pageId));
        if (page.getStatus() == PageStatus.IN_REVIEW) {
            throw new BadRequestException(
                    "Page is IN_REVIEW and cannot be edited until an admin approves or rejects it");
        }
        return page;
    }
}
