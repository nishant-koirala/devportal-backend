package com.fonepay.devportal.modules.cms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.fonepay.devportal.modules.cms.document.AuditLog;
import com.fonepay.devportal.modules.cms.mapper.AuditLogMapper;
import com.fonepay.devportal.modules.cms.repository.AuditLogRepository;
import com.fonepay.devportal.modules.cms.service.impl.AuditLogServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private AuditLogMapper auditLogMapper;
    private Clock clock;
    private AuditLogServiceImpl auditLogService;

    @BeforeEach
    void setUp() {
        auditLogMapper = Mappers.getMapper(AuditLogMapper.class);
        clock = Clock.fixed(Instant.parse("2026-08-25T10:00:00Z"), ZoneId.of("UTC"));
        auditLogService = new AuditLogServiceImpl(auditLogRepository, mongoTemplate, auditLogMapper, clock);
    }

    @Test
    @DisplayName("Log action - success with full context")
    void logAction_success() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(i -> i.getArgument(0));

        AuditLog log = auditLogService.logAction("admin_001", "CREATE_PRODUCT", "prod_999", "PRODUCT", "203.0.113.195");

        assertNotNull(log);
        assertNotNull(log.getId());
        assertEquals("admin_001", log.getAdminId());
        assertEquals("CREATE_PRODUCT", log.getAction());
        assertEquals("prod_999", log.getTargetId());
        assertEquals("PRODUCT", log.getTargetType());
        assertEquals("203.0.113.195", log.getSourceIp());
        assertEquals(Instant.parse("2026-08-25T10:00:00Z"), log.getTimestamp());

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Log action - database failure triggers fallback logging without throwing uncaught exception")
    void logAction_dbFailure_fallbackTriggered() {
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("MongoDB connection timeout"));

        // Should handle gracefully and return the audit log object rather than crashing the calling service
        AuditLog log = auditLogService.logAction("admin_001", "UPDATE_PRODUCT", "prod_999", "PRODUCT", "127.0.0.1");

        assertNotNull(log);
        assertEquals("admin_001", log.getAdminId());
        assertEquals("UPDATE_PRODUCT", log.getAction());
        assertEquals("prod_999", log.getTargetId());
    }
}
