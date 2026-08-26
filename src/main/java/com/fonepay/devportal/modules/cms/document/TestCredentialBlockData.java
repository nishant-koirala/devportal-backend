package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("TEST_CREDENTIAL")
public class TestCredentialBlockData implements BlockData {
    private String environment;
    private String baseUrl;
    private List<Credential> credentials;
    private String usageNotes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Credential {
        private String key;
        private String value;
        private String description;
    }
}
