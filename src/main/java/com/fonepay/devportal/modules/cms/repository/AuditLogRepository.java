package com.fonepay.devportal.modules.cms.repository;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.cms.document.AuditLog;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    Page<AuditLog> findByAdminId(String adminId, Pageable pageable);

    Page<AuditLog> findByTargetId(String targetId, Pageable pageable);

    Page<AuditLog> findByTargetType(String targetType, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(Instant start, Instant end, Pageable pageable);

    Page<AuditLog> findByAdminIdAndTargetType(String adminId, String targetType, Pageable pageable);
}
