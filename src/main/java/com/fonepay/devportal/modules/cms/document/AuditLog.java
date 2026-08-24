package com.fonepay.devportal.modules.cms.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    @Field("admin_id")
    private String adminId;

    @Field("action")
    private String action;

    @Field("target_id")
    private String targetId;

    @Field("timestamp")
    private Instant timestamp;
}
