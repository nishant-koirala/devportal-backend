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
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "page_versions")
@CompoundIndexes({
        @CompoundIndex(name = "page_version_idx", def = "{'page_id': 1, 'version_number': -1}")
})
public class PageVersion {

    @Id
    private String id;

    @Indexed
    @Field("page_id")
    private String pageId;

    @Field("version_number")
    private int versionNumber;

    @Builder.Default
    @Field("published_blocks")
    private List<Block> publishedBlocks = new ArrayList<>();

    @Field("published_at")
    private Instant publishedAt;

    @Field("published_by")
    private String publishedBy;

    @Field("commit_message")
    private String commitMessage;
}
