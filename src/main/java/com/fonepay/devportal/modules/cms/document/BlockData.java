package com.fonepay.devportal.modules.cms.document;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = HeadingBlockData.class, name = "HEADING"),
    @JsonSubTypes.Type(value = ParagraphBlockData.class, name = "PARAGRAPH"),
    @JsonSubTypes.Type(value = CodeBlockData.class, name = "CODE"),
    @JsonSubTypes.Type(value = EndpointBlockData.class, name = "ENDPOINT"),
    @JsonSubTypes.Type(value = FaqBlockData.class, name = "FAQ"),
    @JsonSubTypes.Type(value = TableBlockData.class, name = "TABLE"),
    @JsonSubTypes.Type(value = ImageBlockData.class, name = "IMAGE"),
    @JsonSubTypes.Type(value = NoteWarningBlockData.class, name = "NOTE_WARNING"),
    @JsonSubTypes.Type(value = ParameterTableBlockData.class, name = "PARAMETER_TABLE"),
    @JsonSubTypes.Type(value = TestCredentialBlockData.class, name = "TEST_CREDENTIAL")
})
public interface BlockData {
}
