package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("FAQ") // TEMPORARY FOR DEV 3 TESTING
public class FaqBlockData implements BlockData {
    private String question;
    private String answer;
}
