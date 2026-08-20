package com.fonepay.devportal.modules.user.document;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedRole {

    @Field("role_name")
    private String roleName;

    @Field("assigned_at")
    private Instant assignedAt;

    @Field("assigned_by")
    private String assignedBy;

}
