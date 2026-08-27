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
import com.fonepay.devportal.modules.notification.dto.response.StaffBroadcastResponse;
import com.fonepay.devportal.modules.notification.dto.response.StaffBroadcastSummaryResponse;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.service.BroadcastSseService;
import com.fonepay.devportal.modules.notification.service.StaffBroadcastService;
import com.fonepay.devportal.modules.user.document.User;

@ExtendWith(MockitoExtension.class)
class StaffBroadcastControllerTest {

    @Mock
    private StaffBroadcastService staffBroadcastService;

    @Mock
    private BroadcastSseService broadcastSseService;

    @Mock
    private Authentication authentication;

    private Clock fixedClock;
    private StaffBroadcastController controller;

    private static final Instant FIXED_NOW = Instant.parse("2026-08-27T10:00:00Z");

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(FIXED_NOW, ZoneId.of("UTC"));
        controller = new StaffBroadcastController(staffBroadcastService, broadcastSseService, fixedClock);
    }

    @Test
    void streamBroadcasts_ShouldReturnSseEmitter() {
        User staff = new User();
        staff.setUserId("staff_1");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(staff);

        org.springframework.web.servlet.mvc.method.annotation.SseEmitter mockEmitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter();
        when(broadcastSseService.subscribe(staff)).thenReturn(mockEmitter);

        org.springframework.web.servlet.mvc.method.annotation.SseEmitter result = controller.streamBroadcasts(authentication);

        assertNotNull(result);
        assertEquals(mockEmitter, result);
    }

    @Test
    void getActiveBroadcasts_ShouldReturnActiveList() {
        User staff = new User();
        staff.setUserId("staff_1");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(staff);

        StaffBroadcastResponse r1 = StaffBroadcastResponse.builder().id("bc_1").build();
        when(staffBroadcastService.getActiveBroadcasts(eq(staff), eq(Set.of(BroadcastDisplayMode.GLOBAL_BANNER)), eq(false)))
                .thenReturn(List.of(r1));

        ResponseEntity<ApiResponse<List<StaffBroadcastResponse>>> response = controller.getActiveBroadcasts(
                Set.of(BroadcastDisplayMode.GLOBAL_BANNER), false, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getSummary_ShouldReturnSummary() {
        User staff = new User();
        staff.setUserId("staff_1");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(staff);

        StaffBroadcastSummaryResponse summaryResponse = new StaffBroadcastSummaryResponse(3, 1);
        when(staffBroadcastService.getSummary(staff)).thenReturn(summaryResponse);

        ResponseEntity<ApiResponse<StaffBroadcastSummaryResponse>> response = controller.getSummary(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().getData().getUnreadCount());
        assertEquals(1, response.getBody().getData().getActiveBannerCount());
    }

    @Test
    void markAsRead_ShouldReturnUpdatedBroadcast() {
        User staff = new User();
        staff.setUserId("staff_1");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(staff);

        StaffBroadcastResponse mockResp = StaffBroadcastResponse.builder().id("bc_1").isRead(true).build();
        when(staffBroadcastService.markAsRead("bc_1", "staff_1")).thenReturn(mockResp);

        ResponseEntity<ApiResponse<StaffBroadcastResponse>> response = controller.markAsRead("bc_1", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().getData().isRead());
    }

    @Test
    void markAllAsRead_ShouldInvokeService() {
        User staff = new User();
        staff.setUserId("staff_1");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(staff);

        ResponseEntity<ApiResponse<Void>> response = controller.markAllAsRead(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffBroadcastService).markAllAsRead(staff);
    }

    @Test
    void dismissBroadcast_ShouldReturnDismissedBroadcast() {
        User staff = new User();
        staff.setUserId("staff_1");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(staff);

        StaffBroadcastResponse mockResp = StaffBroadcastResponse.builder().id("bc_1").isDismissed(true).build();
        when(staffBroadcastService.dismissBroadcast("bc_1", "staff_1")).thenReturn(mockResp);

        ResponseEntity<ApiResponse<StaffBroadcastResponse>> response = controller.dismissBroadcast("bc_1", authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().getData().isDismissed());
    }
}
