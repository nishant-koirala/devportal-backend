package com.fonepay.devportal.modules.developer.document;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_bookmarks")
public class UserBookmark {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    private String id;

    @Column(name = "user_id", length = 26, nullable = false)
    private String userId;

    @Column(name = "page_id", length = 26, columnDefinition = "CHAR(26)")
    private String pageId;

    @Column(name = "page_url", length = 2048)
    private String pageUrl;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
