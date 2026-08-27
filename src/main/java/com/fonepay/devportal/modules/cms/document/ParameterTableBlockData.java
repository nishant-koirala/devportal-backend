package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fonepay.devportal.common.util.HtmlSanitizerUtil;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("PARAMETER_TABLE")
public class ParameterTableBlockData implements BlockData {
    private List<Parameter> parameters;

    @Override
    public void sanitize() {
        if (parameters != null) {
            for (Parameter p : parameters) {
                p.setName(HtmlSanitizerUtil.sanitize(p.getName()));
                p.setDescription(HtmlSanitizerUtil.sanitize(p.getDescription()));
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {
        private String name;
        private String type;
        private boolean required;
        private String description;
    }
}
