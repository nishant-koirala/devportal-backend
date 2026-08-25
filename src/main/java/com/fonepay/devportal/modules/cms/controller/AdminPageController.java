package com.fonepay.devportal.modules.cms.controller;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.modules.cms.document.Block;
import com.fonepay.devportal.modules.cms.dto.request.BlockCreateRequest;
import com.fonepay.devportal.modules.cms.dto.request.BlockReorderRequest;
import com.fonepay.devportal.modules.cms.dto.request.BlockUpdateRequest;
import com.fonepay.devportal.modules.cms.service.BlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping(ApiRoutes.Admin.PAGES)
@RequiredArgsConstructor
public class AdminPageController {

    private final BlockService blockService;

    @PostMapping("/{pageId}/blocks")
    public ResponseEntity<ApiResponse<Block>> addBlock(
            @PathVariable String pageId,
            @Valid @RequestBody BlockCreateRequest request) {
            
        Block block = blockService.addBlock(pageId, request.getType(), request.getData(), request.getOrder());
        
        return ResponseEntity.ok(ApiResponse.<Block>builder()
                .success(true)
                .message("Block added successfully")
                .data(block)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PutMapping("/{pageId}/blocks/{blockId}")
    public ResponseEntity<ApiResponse<Void>> updateBlock(
            @PathVariable String pageId,
            @PathVariable String blockId,
            @Valid @RequestBody BlockUpdateRequest request) {
            
        blockService.updateBlockData(pageId, blockId, request.getData(), request.getCurrentVersion());
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Block updated successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PatchMapping("/{pageId}/blocks/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderBlocks(
            @PathVariable String pageId,
            @Valid @RequestBody BlockReorderRequest request) {
            
        blockService.reorderBlocks(pageId, request.getBlockIds());
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Blocks reordered successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/{pageId}/blocks/{blockId}")
    public ResponseEntity<ApiResponse<Void>> deleteBlock(
            @PathVariable String pageId,
            @PathVariable String blockId) {
            
        blockService.deleteBlock(pageId, blockId);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Block deleted successfully")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
