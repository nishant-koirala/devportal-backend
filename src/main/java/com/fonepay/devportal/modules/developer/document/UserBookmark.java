package com.fonepay.devportal.modules.developer.document;

import java.time.Instant;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_bookmarks")
@CompoundIndexes({
        @CompoundIndex(name = "user_page_idx", def = "{'user_id': 1, 'page_id': 1}", unique = true, sparse = true),
        @CompoundIndex(name = "user_page_url_idx", def = "{'user_id': 1, 'page_url': 1}", unique = true, sparse = true),
        @CompoundIndex(name = "user_created_idx", def = "{'user_id': 1, 'created_at': -1}")
})
public class UserBookmark {

    @Id
    private String id;

    @Indexed
    @Field("user_id")
    private String userId;

    @Field("page_id")
    private String pageId;

    @Field("page_url")
    private String pageUrl;

    @Field("title")
    private String title;

    @Field("created_at")
    private Instant createdAt;
}
