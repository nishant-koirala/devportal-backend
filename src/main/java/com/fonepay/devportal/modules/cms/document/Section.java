package com.fonepay.devportal.modules.cms.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sections")
@CompoundIndexes({
    @CompoundIndex(name = "product_slug_idx", def = "{'product_id': 1, 'slug': 1}", unique = true)
})
public class Section {

    @Id
    private String id;

    @Indexed
    @Field("product_id")
    private String productId;

    @Field("name")
    private String name;

    @Field("slug")
    private String slug;

    @Field("order")
    private int order;
}
