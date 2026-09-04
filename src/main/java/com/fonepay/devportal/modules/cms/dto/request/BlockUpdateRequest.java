package com.fonepay.devportal.modules.cms.dto.request;

import com.fonepay.devportal.modules.cms.document.BlockData;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlockUpdateRequest {
    
    @NotNull(message = "Block type is required for deserialization")
    private com.fonepay.devportal.modules.cms.enums.BlockType type;
    
    @NotNull(message = "Block data is required")
    private java.util.Map<String, Object> data;
    
    @NotNull(message = "Current version is required for optimistic locking")
    private Long currentVersion;
}
