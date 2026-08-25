package com.fonepay.devportal.modules.cms.document;

import com.fonepay.devportal.modules.cms.enums.ProductStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
@CompoundIndex(def = "{'status': 1, 'display_order': 1}", name = "idx_products_status_display_order")
public class Product {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("name")
    private String name;

    @Indexed(unique = true)
    @Field("slug")
    private String slug;

    @Field("short_description")
    private String shortDescription;

    @Field("description")
    private String description;

    @Field("logo_url")
    private String logoUrl;

    @Indexed
    @Field("status")
    private ProductStatus status;

    @Field("display_order")
    private int displayOrder;

    @Builder.Default
    @Field("resources")
    private List<ProductResource> resources = new ArrayList<>();

    @Version
    @Field("version")
    private Long version;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;

    @Field("created_by")
    private String createdBy;
}

