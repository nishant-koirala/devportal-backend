package com.fonepay.devportal.modules.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fonepay.devportal.modules.notification.dto.request.CreateBroadcastRequest;
import com.fonepay.devportal.modules.notification.enums.BroadcastPriority;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;
import com.fonepay.devportal.modules.notification.service.impl.BroadcastAdminService;
import com.fonepay.devportal.modules.notification.service.impl.SystemBroadcastServiceImpl;

@ExtendWith(MockitoExtension.class)
class SystemBroadcastServiceTest {

    @Mock
    private BroadcastAdminService broadcastAdminService;

    private SystemBroadcastService systemBroadcastService;

    @BeforeEach
    void setUp() {
        systemBroadcastService = new SystemBroadcastServiceImpl(broadcastAdminService);
    }

    @Test
    void notifyPageReviewSubmitted_ShouldTargetAdminsWithHighPriority() {
        systemBroadcastService.notifyPageReviewSubmitted("page_123", "Authentication Overview", "auth-api", "editor_1");

        ArgumentCaptor<CreateBroadcastRequest> captor = ArgumentCaptor.forClass(CreateBroadcastRequest.class);
        verify(broadcastAdminService).createBroadcast(captor.capture(), eq("SYSTEM"));

        CreateBroadcastRequest request = captor.getValue();
        assertNotNull(request);
        assertEquals(BroadcastTargetRole.ADMINS_ONLY, request.getTargetRole());
        assertEquals(BroadcastPriority.HIGH, request.getPriority());
        assertEquals("/cms/pages/page_123", request.getActionUrl());
    }

    @Test
    void notifyPageApproved_ShouldTargetEditors() {
        systemBroadcastService.notifyPageApproved("page_123", "Authentication Overview", "auth-api", "admin_super");

        ArgumentCaptor<CreateBroadcastRequest> captor = ArgumentCaptor.forClass(CreateBroadcastRequest.class);
        verify(broadcastAdminService).createBroadcast(captor.capture(), eq("SYSTEM"));

        CreateBroadcastRequest request = captor.getValue();
        assertNotNull(request);
        assertEquals(BroadcastTargetRole.CMS_EDITORS, request.getTargetRole());
        assertEquals(BroadcastPriority.NORMAL, request.getPriority());
    }

    @Test
    void notifyPageRejected_ShouldTargetEditorsWithHighPriority() {
        systemBroadcastService.notifyPageRejected("page_123", "Authentication Overview", "auth-api", "Missing payload schema", "admin_super");

        ArgumentCaptor<CreateBroadcastRequest> captor = ArgumentCaptor.forClass(CreateBroadcastRequest.class);
        verify(broadcastAdminService).createBroadcast(captor.capture(), eq("SYSTEM"));

        CreateBroadcastRequest request = captor.getValue();
        assertNotNull(request);
        assertEquals(BroadcastTargetRole.CMS_EDITORS, request.getTargetRole());
        assertEquals(BroadcastPriority.HIGH, request.getPriority());
    }

    @Test
    void notifyProductStatusChanged_ToDeprecated_ShouldSetHighPriority() {
        systemBroadcastService.notifyProductStatusChanged("prod_1", "Fonepay QR v1", "ACTIVE", "DEPRECATED", "admin_super");

        ArgumentCaptor<CreateBroadcastRequest> captor = ArgumentCaptor.forClass(CreateBroadcastRequest.class);
        verify(broadcastAdminService).createBroadcast(captor.capture(), eq("SYSTEM"));

        CreateBroadcastRequest request = captor.getValue();
        assertNotNull(request);
        assertEquals(BroadcastTargetRole.ALL_STAFF, request.getTargetRole());
        assertEquals(BroadcastPriority.HIGH, request.getPriority());
    }
}
