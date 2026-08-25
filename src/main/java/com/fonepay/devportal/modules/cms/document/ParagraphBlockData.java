package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("PARAGRAPH") // TEMPORARY FOR DEV 3 TESTING
public class ParagraphBlockData implements BlockData {
    private String text;
}
