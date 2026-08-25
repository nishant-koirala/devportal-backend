package com.fonepay.devportal.modules.cms.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BlockReorderRequest {
    
    @NotEmpty(message = "Block IDs list cannot be empty")
    private List<String> blockIds;
}
