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
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "revisions")
@CompoundIndexes({
    @CompoundIndex(name = "page_version_idx", def = "{'page_id': 1, 'version': 1}", unique = true)
})
public class Revision {

    @Id
    private String id;

    @Indexed
    @Field("page_id")
    private String pageId;

    @Field("version")
    private int version;

    @Field("blocks_snapshot")
    private List<Block> blocksSnapshot;

    @Field("created_by")
    private String createdBy;

    @Field("created_at")
    private Instant createdAt;

    @Field("commit_message")
    private String commitMessage;
}
