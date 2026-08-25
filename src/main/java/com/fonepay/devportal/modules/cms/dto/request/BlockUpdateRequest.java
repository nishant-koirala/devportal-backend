package com.fonepay.devportal.modules.cms.dto.request;

import com.fonepay.devportal.modules.cms.document.BlockData;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlockUpdateRequest {
    
    @NotNull(message = "Block type is required for deserialization")
    private com.fonepay.devportal.modules.cms.enums.BlockType type;
    
    @NotNull(message = "Block data is required")
    @com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, include = com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", visible = true)
    private BlockData data;
    
    @NotNull(message = "Current version is required for optimistic locking")
    private Long currentVersion;
}
