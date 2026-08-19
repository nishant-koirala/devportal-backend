package com.fonepay.devportal.modules.user.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
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
@Document(collection = "user_roles")
public class UserRole {
    @Id
    private String id;

    @Indexed
    @Field("user_id")
    private String userId;

    @Indexed
    @Field("role_id")
    private String roleId;

    @Field("assigned_by")
    private String assignedBy;

    @Field("assigned_at")
    private Instant assignedAt;
}
