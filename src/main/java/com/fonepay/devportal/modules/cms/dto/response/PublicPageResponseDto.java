package com.fonepay.devportal.modules.cms.dto.response;

import java.time.Instant;
import java.util.List;

import com.fonepay.devportal.modules.cms.document.Block;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicPageResponseDto {
    private String id;
    private String productId;
    private String parentId;
    private int pageOrder;
    private String title;
    private String slug;
    private List<Block> publishedBlocks;
    private Instant lastPublishedAt;
    private boolean isAddPrompt;
}
