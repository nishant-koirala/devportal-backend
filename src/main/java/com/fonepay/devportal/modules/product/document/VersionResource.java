package com.fonepay.devportal.modules.product.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Snapshotted developer asset/resource associated with a specific major product release
 * (e.g., SDKs, Postman collections, OpenAPI specifications, sample code).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionResource {

    @Field("resource_id")
    private String resourceId;

    @Field("name")
    private String name;

    @Field("resource_type")
    private String resourceType; // e.g., "SDK", "POSTMAN", "SAMPLE_CODE", "OPENAPI_SPEC"

    @Field("url")
    private String url;

    @Field("version")
    private String version;

    @Field("display_order")
    private int displayOrder;

    @JsonProperty("isActive")
    @Field("is_active")
    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
