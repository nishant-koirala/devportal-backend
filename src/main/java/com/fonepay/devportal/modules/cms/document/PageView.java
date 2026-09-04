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
@Document(collection = "page_views")
@CompoundIndexes({
    @CompoundIndex(name = "developer_page_time_idx", def = "{'developer_id': 1, 'page_id': 1, 'viewed_at': -1}")
})
public class PageView {

    @Id
    private String id;

    @Indexed
    @Field("page_id")
    private String pageId;

    @Indexed
    @Field("developer_id")
    private String developerId;

    @Field("viewed_at")
    private Instant viewedAt;

    @Field("user_agent")
    private String userAgent;

    @Field("ip_address")
    private String ipAddress;
}
