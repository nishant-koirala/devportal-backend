package com.fonepay.devportal.modules.developer.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBookmarkResponse {

    private String id;
    private String userId;
    private String pageId;
    private String pageUrl;
    private String title;
    private Instant createdAt;
}
