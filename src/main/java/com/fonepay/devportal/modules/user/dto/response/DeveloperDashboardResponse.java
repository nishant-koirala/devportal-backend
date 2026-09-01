package com.fonepay.devportal.modules.user.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fonepay.devportal.modules.cms.dto.response.PublicProductResponseDto;
import com.fonepay.devportal.modules.developer.dto.response.UserBookmarkResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeveloperDashboardResponse {

    private UserProfileResponse profile;
    private List<PublicProductResponseDto> subscribedProducts;
    private List<UserBookmarkResponse> bookmarks;
}
