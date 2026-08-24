package com.fonepay.devportal.modules.cms.document;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = HeadingBlockData.class, name = "HEADING"),
    @JsonSubTypes.Type(value = ParagraphBlockData.class, name = "PARAGRAPH"),
    @JsonSubTypes.Type(value = CodeBlockData.class, name = "CODE"),
    @JsonSubTypes.Type(value = EndpointBlockData.class, name = "ENDPOINT"),
    @JsonSubTypes.Type(value = FaqBlockData.class, name = "FAQ")
})
public interface BlockData {
}
