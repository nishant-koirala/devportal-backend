package com.fonepay.devportal.modules.notification.service;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.enums.BroadcastStatus;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;
import com.fonepay.devportal.modules.notification.mapper.BroadcastMapper;
import com.fonepay.devportal.modules.notification.service.impl.BroadcastSseServiceImpl;
import com.fonepay.devportal.modules.user.document.AssignedRole;
import com.fonepay.devportal.modules.user.document.User;

@ExtendWith(MockitoExtension.class)
class BroadcastSseServiceTest {

    private BroadcastMapper broadcastMapper;
    private BroadcastSseService sseService;

    @BeforeEach
    void setUp() {
        broadcastMapper = new BroadcastMapper();
        sseService = new BroadcastSseServiceImpl(broadcastMapper);
    }

    @Test
    void subscribe_ShouldReturnEmitterAndIncrementCount() {
        User user = new User();
        user.setUserId("user_101");
        user.setRoles(List.of(AssignedRole.builder().roleName("EDITOR").build()));

        SseEmitter emitter = sseService.subscribe(user);

        assertNotNull(emitter);
        assertEquals(1, sseService.getActiveConnectionCount());
    }

    @Test
    void subscribe_MultipleTabsForSameUser_ShouldTrackAllEmitters() {
        User user = new User();
        user.setUserId("user_101");

        SseEmitter emitter1 = sseService.subscribe(user);
        SseEmitter emitter2 = sseService.subscribe(user);

        assertNotNull(emitter1);
        assertNotNull(emitter2);
        assertEquals(2, sseService.getActiveConnectionCount());
    }

    @Test
    void sendBroadcast_ShouldDispatchWithoutException() {
        User admin = new User();
        admin.setUserId("admin_user");
        admin.setRoles(List.of(AssignedRole.builder().roleName("ADMIN").build()));

        sseService.subscribe(admin);

        Broadcast broadcast = Broadcast.builder()
                .id("bc_1")
                .title("Urgent System Alert")
                .message("Immediate doc review required")
                .targetRole(BroadcastTargetRole.ADMINS_ONLY)
                .displayModes(Set.of(BroadcastDisplayMode.GLOBAL_BANNER))
                .status(BroadcastStatus.ACTIVE)
                .build();

        // Should successfully send to the admin without exceptions
        sseService.sendBroadcast(broadcast, "BROADCAST_CREATED");
        assertEquals(1, sseService.getActiveConnectionCount());
    }

    @Test
    void sendCancellation_ShouldDispatchWithoutException() {
        User editor = new User();
        editor.setUserId("editor_user");
        editor.setRoles(List.of(AssignedRole.builder().roleName("EDITOR").build()));

        sseService.subscribe(editor);

        sseService.sendCancellation("bc_cancelled_1");
        assertEquals(1, sseService.getActiveConnectionCount());
    }

    @Test
    void sendHeartbeat_ShouldSendPingToAllConnections() {
        User user = new User();
        user.setUserId("user_heartbeat");

        sseService.subscribe(user);
        sseService.sendHeartbeat();

        assertEquals(1, sseService.getActiveConnectionCount());
    }
}
