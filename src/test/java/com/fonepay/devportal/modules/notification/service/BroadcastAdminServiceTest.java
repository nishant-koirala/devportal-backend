package com.fonepay.devportal.modules.notification.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.dto.request.CreateBroadcastRequest;
import com.fonepay.devportal.modules.notification.dto.request.UpdateBroadcastRequest;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastMetricsResponse;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastResponse;
import com.fonepay.devportal.modules.notification.enums.BroadcastCategory;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.enums.BroadcastPriority;
import com.fonepay.devportal.modules.notification.enums.BroadcastStatus;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;
import com.fonepay.devportal.modules.notification.mapper.BroadcastMapper;
import com.fonepay.devportal.modules.notification.repository.BroadcastRepository;
import com.fonepay.devportal.modules.notification.repository.UserBroadcastInteractionRepository;
import com.fonepay.devportal.modules.notification.service.impl.BroadcastAdminService;
import com.fonepay.devportal.modules.notification.service.impl.BroadcastAdminServiceImpl;

@ExtendWith(MockitoExtension.class)
class BroadcastAdminServiceTest {

    @Mock
    private BroadcastRepository broadcastRepository;

    @Mock
    private UserBroadcastInteractionRepository interactionRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private BroadcastMapper broadcastMapper;
    private Clock fixedClock;
    private BroadcastAdminService broadcastAdminService;

    private static final Instant FIXED_NOW = Instant.parse("2026-08-27T10:00:00Z");

    @BeforeEach
    void setUp() {
        broadcastMapper = new BroadcastMapper();
        fixedClock = Clock.fixed(FIXED_NOW, ZoneId.of("UTC"));
        broadcastAdminService = new BroadcastAdminServiceImpl(
                broadcastRepository,
                interactionRepository,
                broadcastMapper,
                mongoTemplate,
                eventPublisher,
                fixedClock);
    }

    @Test
    void createBroadcast_ShouldSaveAndReturnResponse() {
        CreateBroadcastRequest request = CreateBroadcastRequest.builder()
                .title("Planned Maintenance")
                .message("Sandbox downtime tonight at 10 PM")
                .targetRole(BroadcastTargetRole.ALL_STAFF)
                .displayModes(Set.of(BroadcastDisplayMode.GLOBAL_BANNER))
                .priority(BroadcastPriority.HIGH)
                .category(BroadcastCategory.MAINTENANCE)
                .isDismissible(true)
                .startsAt(FIXED_NOW)
                .expiresAt(FIXED_NOW.plusSeconds(3600))
                .build();

        when(broadcastRepository.save(any(Broadcast.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BroadcastResponse response = broadcastAdminService.createBroadcast(request, "admin_123");

        assertNotNull(response);
        assertEquals("Planned Maintenance", response.getTitle());
        assertEquals(BroadcastTargetRole.ALL_STAFF, response.getTargetRole());
        assertEquals(BroadcastStatus.ACTIVE, response.getStatus());
        assertEquals("admin_123", response.getCreatedBy());
        verify(broadcastRepository).save(any(Broadcast.class));
    }

    @Test
    void createBroadcast_WithInvalidExpiry_ShouldThrowBadRequest() {
        CreateBroadcastRequest request = CreateBroadcastRequest.builder()
                .title("Invalid Timing")
                .message("Expiry before start")
                .targetRole(BroadcastTargetRole.CMS_EDITORS)
                .displayModes(Set.of(BroadcastDisplayMode.NOTIFICATION_BELL))
                .startsAt(FIXED_NOW.plusSeconds(3600))
                .expiresAt(FIXED_NOW)
                .build();

        assertThrows(BadRequestException.class, () -> broadcastAdminService.createBroadcast(request, "admin_123"));
    }

    @Test
    void updateBroadcast_ShouldUpdateFieldsAndSave() {
        Broadcast existing = Broadcast.builder()
                .id("bc_1")
                .title("Old Title")
                .message("Old Message")
                .targetRole(BroadcastTargetRole.ALL_STAFF)
                .displayModes(Set.of(BroadcastDisplayMode.GLOBAL_BANNER))
                .status(BroadcastStatus.ACTIVE)
                .startsAt(FIXED_NOW)
                .build();

        when(broadcastRepository.findById("bc_1")).thenReturn(Optional.of(existing));
        when(broadcastRepository.save(any(Broadcast.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateBroadcastRequest updateRequest = UpdateBroadcastRequest.builder()
                .title("New Updated Title")
                .priority(BroadcastPriority.URGENT)
                .build();

        BroadcastResponse response = broadcastAdminService.updateBroadcast("bc_1", updateRequest, "admin_123");

        assertNotNull(response);
        assertEquals("New Updated Title", response.getTitle());
        assertEquals(BroadcastPriority.URGENT, response.getPriority());
    }

    @Test
    void cancelBroadcast_ShouldSetStatusCancelled() {
        Broadcast existing = Broadcast.builder()
                .id("bc_1")
                .title("Active Broadcast")
                .status(BroadcastStatus.ACTIVE)
                .build();

        when(broadcastRepository.findById("bc_1")).thenReturn(Optional.of(existing));
        when(broadcastRepository.save(any(Broadcast.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BroadcastResponse response = broadcastAdminService.cancelBroadcast("bc_1", "admin_123");

        assertNotNull(response);
        assertEquals(BroadcastStatus.CANCELLED, response.getStatus());
    }

    @Test
    void getBroadcastById_NotFound_ShouldThrowResourceNotFoundException() {
        when(broadcastRepository.findById("missing_id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> broadcastAdminService.getBroadcastById("missing_id"));
    }

    @Test
    void getBroadcastMetrics_ShouldReturnCounts() {
        Broadcast existing = Broadcast.builder().id("bc_1").build();
        when(broadcastRepository.findById("bc_1")).thenReturn(Optional.of(existing));
        when(interactionRepository.countByBroadcastIdAndIsReadTrue("bc_1")).thenReturn(15L);
        when(interactionRepository.countByBroadcastIdAndIsDismissedTrue("bc_1")).thenReturn(7L);

        BroadcastMetricsResponse metrics = broadcastAdminService.getBroadcastMetrics("bc_1");

        assertNotNull(metrics);
        assertEquals("bc_1", metrics.getBroadcastId());
        assertEquals(15L, metrics.getTotalReadCount());
        assertEquals(7L, metrics.getTotalDismissedCount());
    }
}
