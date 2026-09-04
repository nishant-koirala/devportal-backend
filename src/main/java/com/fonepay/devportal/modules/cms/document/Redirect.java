package com.fonepay.devportal.modules.cms.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "redirects")
@CompoundIndexes({
    @CompoundIndex(name = "old_path_idx", def = "{'old_path': 1}", unique = true)
})
public class Redirect {

    @Id
    private String id;

    @Indexed
    @Field("product_id")
    private String productId;

    @Field("page_id")
    private String pageId;

    @Field("old_path")
    private String oldPath;

    @Field("new_path")
    private String newPath;

    @Field("created_at")
    private Instant createdAt;
}
