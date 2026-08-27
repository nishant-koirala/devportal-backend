package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fonepay.devportal.common.util.HtmlSanitizerUtil;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("NOTE_WARNING")
public class NoteWarningBlockData implements BlockData {
    private String type; // NOTE, WARNING, INFO, TIP
    private String content;

    @Override
    public void sanitize() {
        this.content = HtmlSanitizerUtil.sanitize(this.content);
    }
}
