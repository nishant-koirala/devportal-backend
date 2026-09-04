package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import com.fonepay.devportal.common.util.HtmlSanitizerUtil;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("ENDPOINT")
public class EndpointBlockData implements BlockData {
    private String method;
    private String path;
    private String description;
    
    private String authRequirement;
    private List<ParameterTableBlockData.Parameter> requestParams;
    private List<ParameterTableBlockData.Parameter> responseFields;
    private Map<String, String> errorCodes;
    private String implementationNotes;
    private String curlSample;
    private String responseExample;
    private String errorExample;

    @Override
    public void sanitize() {
        this.description = HtmlSanitizerUtil.sanitize(this.description);
        this.implementationNotes = HtmlSanitizerUtil.sanitize(this.implementationNotes);
        
        if (requestParams != null) {
            requestParams.forEach(p -> {
                p.setName(HtmlSanitizerUtil.sanitize(p.getName()));
                p.setDescription(HtmlSanitizerUtil.sanitize(p.getDescription()));
            });
        }
        
        if (responseFields != null) {
            responseFields.forEach(p -> {
                p.setName(HtmlSanitizerUtil.sanitize(p.getName()));
                p.setDescription(HtmlSanitizerUtil.sanitize(p.getDescription()));
            });
        }
    }
}
