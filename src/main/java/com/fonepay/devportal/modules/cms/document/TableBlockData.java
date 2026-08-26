package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("TABLE")
public class TableBlockData implements BlockData {
    private List<List<String>> rows;
    private boolean hasHeaderRow;
}
