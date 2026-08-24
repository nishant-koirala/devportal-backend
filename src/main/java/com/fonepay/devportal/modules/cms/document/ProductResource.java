package com.fonepay.devportal.modules.cms.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResource {
    private String resourceId;
    private String name;
    private String resourceType;
    private String url;
    private int displayOrder;
    private boolean isActive;
}
