package com.fonepay.devportal.modules.cms.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.fonepay.devportal.modules.cms.enums.PageStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageTreeNodeResponse {

    private String id;
    private String parentId;
    private String title;
    private String slug;
    private PageStatus status;
    private int pageOrder;

    @Builder.Default
    private List<PageTreeNodeResponse> children = new ArrayList<>();
}
