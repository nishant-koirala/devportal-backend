package com.fonepay.devportal.modules.cms.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "page_versions")
public class PageVersion {

    @Id
    private String id;

    @Indexed
    @Field("page_id")
    private String pageId;

    @Field("version_number")
    private int versionNumber;

    @Field("published_blocks")
    private List<Block> publishedBlocks = new ArrayList<>();

    @Field("published_at")
    private Instant publishedAt;

    @Field("published_by")
    private String publishedBy;

    @Field("commit_message")
    private String commitMessage;
}
