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

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "audit_logs")
@CompoundIndexes({
    @CompoundIndex(def = "{'admin_id': 1, 'timestamp': -1}", name = "idx_audit_logs_admin_timestamp"),
    @CompoundIndex(def = "{'target_id': 1, 'target_type': 1}", name = "idx_audit_logs_target")
})
public class AuditLog {

    @Id
    private String id;

    @Indexed
    @Field("admin_id")
    private String adminId;

    @Field("action")
    private String action;

    @Indexed
    @Field("target_id")
    private String targetId;

    @Field("target_type")
    private String targetType;

    @Field("source_ip")
    private String sourceIp;

    @Indexed
    @Field("timestamp")
    private Instant timestamp;
}

