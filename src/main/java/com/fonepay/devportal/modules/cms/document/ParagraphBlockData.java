package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fonepay.devportal.common.util.HtmlSanitizerUtil;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("PARAGRAPH")
public class ParagraphBlockData implements BlockData {
    private String text;

    @Override
    public void sanitize() {
        this.text = HtmlSanitizerUtil.sanitize(this.text);
    }
}
