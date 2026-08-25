package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("CODE") // TEMPORARY FOR DEV 3 TESTING
public class CodeBlockData implements BlockData {
    private String language;
    private String code;
}
