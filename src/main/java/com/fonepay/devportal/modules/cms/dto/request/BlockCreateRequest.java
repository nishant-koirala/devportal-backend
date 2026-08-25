package com.fonepay.devportal.modules.cms.dto.request;

import com.fonepay.devportal.modules.cms.document.BlockData;
import com.fonepay.devportal.modules.cms.enums.BlockType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlockCreateRequest {
    
    @NotNull(message = "Block type is required")
    private BlockType type;
    
    @NotNull(message = "Block data is required")
    @com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, include = com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", visible = true)
    private BlockData data;
    
    private Integer order;
}
