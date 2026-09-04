package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fonepay.devportal.common.util.HtmlSanitizerUtil;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("TEST_CREDENTIAL")
public class TestCredentialBlockData implements BlockData {
    private String environment;
    private String baseUrl;
    private List<Credential> credentials;
    private String usageNotes;

    @Override
    public void sanitize() {
        this.usageNotes = HtmlSanitizerUtil.sanitize(this.usageNotes);
        if (credentials != null) {
            for (Credential c : credentials) {
                c.setDescription(HtmlSanitizerUtil.sanitize(c.getDescription()));
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Credential {
        private String key;
        private String value;
        private String description;
        private boolean isSensitive;
    }
}
