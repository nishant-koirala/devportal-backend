package com.fonepay.devportal.modules.cms.document;

import com.fonepay.devportal.modules.cms.enums.BlockType;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Block {

    @Field("id")
    private String id;

    @Field("type")
    private BlockType type;

    @Field("order")
    private int order;

    @Field("block_version")
    private long blockVersion = 0L;

    @Field("data")
    private BlockData data;
}
