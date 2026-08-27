package com.fonepay.devportal.modules.notification.controller;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.dto.PageResponse;
import com.fonepay.devportal.modules.notification.dto.request.BroadcastFilterRequest;
import com.fonepay.devportal.modules.notification.dto.request.CreateBroadcastRequest;
import com.fonepay.devportal.modules.notification.dto.request.UpdateBroadcastRequest;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastMetricsResponse;
import com.fonepay.devportal.modules.notification.dto.response.BroadcastResponse;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.enums.BroadcastPriority;
import com.fonepay.devportal.modules.notification.enums.BroadcastStatus;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;
import com.fonepay.devportal.modules.notification.service.BroadcastAdminService;
import com.fonepay.devportal.modules.user.document.User;

@ExtendWith(MockitoExtension.class)
class AdminBroadcastControllerTest {

    @Mock
    private BroadcastAdminService broadcastAdminService;

    @Mock
    private Authentication authentication;

    private Clock fixedClock;
    private AdminBroadcastController controller;

    private static final Instant FIXED_NOW = Instant.parse("2026-08-27T10:00:00Z");

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(FIXED_NOW, ZoneId.of("UTC"));
        controller = new AdminBroadcastController(broadcastAdminService, fixedClock);
    }

    @Test
    void createBroadcast_ShouldReturnCreatedResponse() {
        User adminUser = new User();
        adminUser.setUserId("admin_007");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(adminUser);

        CreateBroadcastRequest request = CreateBroadcastRequest.builder()
                .title("Maintenance")
                .message("Tonight")
                .targetRole(BroadcastTargetRole.ALL_STAFF)
                .displayModes(Set.of(BroadcastDisplayMode.GLOBAL_BANNER))
                .build();

        BroadcastResponse mockResponse = BroadcastResponse.builder()
                .id("bc_1")
                .title("Maintenance")
                .status(BroadcastStatus.ACTIVE)
                .build();

        when(broadcastAdminService.createBroadcast(eq(request), eq("admin_007"))).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<BroadcastResponse>> result = controller.createBroadcast(request, authentication);

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("bc_1", result.getBody().getData().getId());
    }

    @Test
    void getBroadcasts_ShouldReturnPageResponse() {
        BroadcastFilterRequest filter = new BroadcastFilterRequest();
        PageResponse<BroadcastResponse> pageResponse = PageResponse.<BroadcastResponse>builder()
                .content(List.of(BroadcastResponse.builder().id("bc_1").build()))
                .page(0)
                .size(20)
                .totalElements(1)
                .build();

        when(broadcastAdminService.getBroadcasts(any(BroadcastFilterRequest.class))).thenReturn(pageResponse);

        ResponseEntity<ApiResponse<PageResponse<BroadcastResponse>>> result = controller.getBroadcasts(filter);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().getData().getTotalElements());
    }

    @Test
    void updateBroadcast_ShouldReturnUpdatedResponse() {
        User adminUser = new User();
        adminUser.setUserId("admin_007");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(adminUser);

        UpdateBroadcastRequest request = UpdateBroadcastRequest.builder().priority(BroadcastPriority.URGENT).build();
        BroadcastResponse mockResponse = BroadcastResponse.builder().id("bc_1").priority(BroadcastPriority.URGENT).build();

        when(broadcastAdminService.updateBroadcast(eq("bc_1"), eq(request), eq("admin_007"))).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<BroadcastResponse>> result = controller.updateBroadcast("bc_1", request, authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(BroadcastPriority.URGENT, result.getBody().getData().getPriority());
    }

    @Test
    void cancelBroadcast_ShouldReturnCancelledResponse() {
        User adminUser = new User();
        adminUser.setUserId("admin_007");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(adminUser);

        BroadcastResponse mockResponse = BroadcastResponse.builder().id("bc_1").status(BroadcastStatus.CANCELLED).build();
        when(broadcastAdminService.cancelBroadcast(eq("bc_1"), eq("admin_007"))).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<BroadcastResponse>> result = controller.cancelBroadcast("bc_1", authentication);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(BroadcastStatus.CANCELLED, result.getBody().getData().getStatus());
    }

    @Test
    void getBroadcastMetrics_ShouldReturnMetrics() {
        BroadcastMetricsResponse mockResponse = BroadcastMetricsResponse.builder()
                .broadcastId("bc_1")
                .totalReadCount(20)
                .totalDismissedCount(5)
                .build();

        when(broadcastAdminService.getBroadcastMetrics("bc_1")).thenReturn(mockResponse);

        ResponseEntity<ApiResponse<BroadcastMetricsResponse>> result = controller.getBroadcastMetrics("bc_1");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(20, result.getBody().getData().getTotalReadCount());
    }
}
