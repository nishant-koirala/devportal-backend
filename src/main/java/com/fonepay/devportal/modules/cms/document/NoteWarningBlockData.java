package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("NOTE_WARNING")
public class NoteWarningBlockData implements BlockData {
    private String type; // NOTE, WARNING, INFO, TIP
    private String content;
}
