package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fonepay.devportal.common.util.HtmlSanitizerUtil;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("FAQ") // TEMPORARY FOR DEV 3 TESTING
public class FaqBlockData implements BlockData {
    private String question;
    private String answer;

    @Override
    public void sanitize() {
        this.question = HtmlSanitizerUtil.sanitize(this.question);
        this.answer = HtmlSanitizerUtil.sanitize(this.answer);
    }
}
