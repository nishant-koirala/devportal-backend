package com.fonepay.devportal.modules.cms.dto.request;

import com.fonepay.devportal.modules.cms.document.BlockData;
import com.fonepay.devportal.modules.cms.enums.BlockType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockDto {

    private String id;

    @NotNull(message = "Block type is required")
    private BlockType type;

    @NotNull(message = "Order is required")
    private Integer order;

    @NotNull(message = "Block data is required")
    private java.util.Map<String, Object> data;
}
