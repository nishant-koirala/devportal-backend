package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fonepay.devportal.common.util.HtmlSanitizerUtil;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("ENDPOINT") // TEMPORARY FOR DEV 3 TESTING
public class EndpointBlockData implements BlockData {
    private String method;
    private String path;
    private String description;

    @Override
    public void sanitize() {
        this.description = HtmlSanitizerUtil.sanitize(this.description);
    }
}
