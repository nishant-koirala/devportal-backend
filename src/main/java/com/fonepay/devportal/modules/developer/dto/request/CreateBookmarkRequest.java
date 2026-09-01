package com.fonepay.devportal.modules.developer.dto.request;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookmarkRequest {

    private String pageId;

    private String pageUrl;

    private String title;

    @AssertTrue(message = "Either pageId or pageUrl must be provided")
    public boolean isValidPageReference() {
        return (pageId != null && !pageId.isBlank()) || (pageUrl != null && !pageUrl.isBlank());
    }
}
