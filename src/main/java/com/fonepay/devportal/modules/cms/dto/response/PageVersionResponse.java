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
public class PageVersionResponse {

    private String id;
    private String pageId;
    private int versionNumber;
    private List<Block> publishedBlocks;
    private Instant publishedAt;
    private String publishedBy;
    private String commitMessage;
}
