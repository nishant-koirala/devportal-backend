package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fonepay.devportal.common.util.HtmlSanitizerUtil;

@Data
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.annotation.TypeAlias("TABLE")
public class TableBlockData implements BlockData {
    private List<List<String>> rows;
    private boolean hasHeaderRow;

    @Override
    public void sanitize() {
        if (rows != null) {
            for (List<String> row : rows) {
                if (row != null) {
                    for (int i = 0; i < row.size(); i++) {
                        row.set(i, HtmlSanitizerUtil.sanitize(row.get(i)));
                    }
                }
            }
        }
    }
}
