package com.fonepay.devportal.modules.notification.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.document.UserBroadcastInteraction;
import com.fonepay.devportal.modules.notification.dto.response.StaffBroadcastResponse;
import com.fonepay.devportal.modules.notification.dto.response.StaffBroadcastSummaryResponse;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.enums.BroadcastStatus;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;
import com.fonepay.devportal.modules.notification.mapper.BroadcastMapper;
import com.fonepay.devportal.modules.notification.repository.BroadcastRepository;
import com.fonepay.devportal.modules.notification.repository.UserBroadcastInteractionRepository;
import com.fonepay.devportal.modules.notification.service.impl.StaffBroadcastServiceImpl;
import com.fonepay.devportal.modules.user.document.AssignedRole;
import com.fonepay.devportal.modules.user.document.User;

@ExtendWith(MockitoExtension.class)
class StaffBroadcastServiceTest {

    @Mock
    private BroadcastRepository broadcastRepository;

    @Mock
    private UserBroadcastInteractionRepository interactionRepository;

    private BroadcastMapper broadcastMapper;
    private Clock fixedClock;
    private StaffBroadcastService staffBroadcastService;

    private static final Instant FIXED_NOW = Instant.parse("2026-08-27T10:00:00Z");

    @BeforeEach
    void setUp() {
        broadcastMapper = new BroadcastMapper();
        fixedClock = Clock.fixed(FIXED_NOW, ZoneId.of("UTC"));
        staffBroadcastService = new StaffBroadcastServiceImpl(
                broadcastRepository,
                interactionRepository,
                broadcastMapper,
                fixedClock);
    }

    @Test
    void getActiveBroadcasts_ForEditorUser_ShouldFetchMatchingRoles() {
        User editor = new User();
        editor.setUserId("user_editor_1");
        editor.setRoles(List.of(AssignedRole.builder().roleName("EDITOR").build()));

        Broadcast b1 = Broadcast.builder()
                .id("bc_1")
                .title("Content Review Guidelines")
                .targetRole(BroadcastTargetRole.CMS_EDITORS)
                .displayModes(Set.of(BroadcastDisplayMode.NOTIFICATION_BELL))
                .status(BroadcastStatus.ACTIVE)
                .startsAt(FIXED_NOW.minusSeconds(100))
                .build();

        Broadcast b2 = Broadcast.builder()
                .id("bc_2")
                .title("System-wide Alert")
                .targetRole(BroadcastTargetRole.ALL_STAFF)
                .displayModes(Set.of(BroadcastDisplayMode.GLOBAL_BANNER))
                .status(BroadcastStatus.ACTIVE)
                .startsAt(FIXED_NOW.minusSeconds(200))
                .build();

        when(broadcastRepository.findActiveForRoles(eq(BroadcastStatus.ACTIVE), anyCollection(), eq(FIXED_NOW)))
                .thenReturn(List.of(b1, b2));

        UserBroadcastInteraction interaction1 = UserBroadcastInteraction.builder()
                .broadcastId("bc_1")
                .userId("user_editor_1")
                .isRead(true)
                .readAt(FIXED_NOW)
                .build();

        when(interactionRepository.findAllByUserIdAndBroadcastIdIn(eq("user_editor_1"), anyCollection()))
                .thenReturn(List.of(interaction1));

        List<StaffBroadcastResponse> results = staffBroadcastService.getActiveBroadcasts(editor, null, false);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getId().equals("bc_1") && r.isRead()));
        assertTrue(results.stream().anyMatch(r -> r.getId().equals("bc_2") && !r.isRead()));
    }

    @Test
    void getActiveBroadcasts_WithDisplayModeFilter_ShouldFilterCorrectly() {
        User admin = new User();
        admin.setUserId("user_admin_1");
        admin.setRoles(List.of(AssignedRole.builder().roleName("ADMIN").build()));

        Broadcast bannerBc = Broadcast.builder()
                .id("bc_banner")
                .title("Banner Announcement")
                .displayModes(Set.of(BroadcastDisplayMode.GLOBAL_BANNER))
                .status(BroadcastStatus.ACTIVE)
                .build();

        Broadcast bellBc = Broadcast.builder()
                .id("bc_bell")
                .title("Bell Announcement")
                .displayModes(Set.of(BroadcastDisplayMode.NOTIFICATION_BELL))
                .status(BroadcastStatus.ACTIVE)
                .build();

        when(broadcastRepository.findActiveForRoles(eq(BroadcastStatus.ACTIVE), anyCollection(), eq(FIXED_NOW)))
                .thenReturn(List.of(bannerBc, bellBc));
        when(interactionRepository.findAllByUserIdAndBroadcastIdIn(eq("user_admin_1"), anyCollection()))
                .thenReturn(List.of());

        List<StaffBroadcastResponse> results = staffBroadcastService.getActiveBroadcasts(
                admin, Set.of(BroadcastDisplayMode.GLOBAL_BANNER), false);

        assertEquals(1, results.size());
        assertEquals("bc_banner", results.get(0).getId());
    }

    @Test
    void getSummary_ShouldCalculateUnreadAndBannerCounts() {
        User editor = new User();
        editor.setUserId("user_editor_1");
        editor.setRoles(List.of(AssignedRole.builder().roleName("EDITOR").build()));

        Broadcast b1 = Broadcast.builder()
                .id("bc_1")
                .displayModes(Set.of(BroadcastDisplayMode.GLOBAL_BANNER, BroadcastDisplayMode.NOTIFICATION_BELL))
                .build();

        Broadcast b2 = Broadcast.builder()
                .id("bc_2")
                .displayModes(Set.of(BroadcastDisplayMode.NOTIFICATION_BELL))
                .build();

        when(broadcastRepository.findActiveForRoles(eq(BroadcastStatus.ACTIVE), anyCollection(), eq(FIXED_NOW)))
                .thenReturn(List.of(b1, b2));

        // b1 is read and dismissed; b2 is unread and not dismissed
        UserBroadcastInteraction i1 = UserBroadcastInteraction.builder()
                .broadcastId("bc_1")
                .isRead(true)
                .isDismissed(true)
                .build();

        when(interactionRepository.findAllByUserIdAndBroadcastIdIn(eq("user_editor_1"), anyCollection()))
                .thenReturn(List.of(i1));

        StaffBroadcastSummaryResponse summary = staffBroadcastService.getSummary(editor);

        assertNotNull(summary);
        assertEquals(1, summary.getUnreadCount()); // b2 is unread
        assertEquals(0, summary.getActiveBannerCount()); // b1 is banner but dismissed
    }

    @Test
    void markAsRead_ShouldSaveReadState() {
        Broadcast broadcast = Broadcast.builder()
                .id("bc_1")
                .title("Test Broadcast")
                .build();

        when(broadcastRepository.findById("bc_1")).thenReturn(Optional.of(broadcast));
        when(interactionRepository.findByUserIdAndBroadcastId("user_1", "bc_1")).thenReturn(Optional.empty());
        when(interactionRepository.save(any(UserBroadcastInteraction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StaffBroadcastResponse response = staffBroadcastService.markAsRead("bc_1", "user_1");

        assertNotNull(response);
        assertTrue(response.isRead());
        assertEquals(FIXED_NOW, response.getReadAt());
        verify(interactionRepository).save(any(UserBroadcastInteraction.class));
    }

    @Test
    void dismissBroadcast_WhenNotDismissible_ShouldThrowBadRequest() {
        Broadcast broadcast = Broadcast.builder()
                .id("bc_mandatory")
                .isDismissible(false)
                .build();

        when(broadcastRepository.findById("bc_mandatory")).thenReturn(Optional.of(broadcast));

        assertThrows(BadRequestException.class, () -> staffBroadcastService.dismissBroadcast("bc_mandatory", "user_1"));
    }

    @Test
    void dismissBroadcast_WhenDismissible_ShouldSaveDismissedState() {
        Broadcast broadcast = Broadcast.builder()
                .id("bc_dismissible")
                .isDismissible(true)
                .build();

        when(broadcastRepository.findById("bc_dismissible")).thenReturn(Optional.of(broadcast));
        when(interactionRepository.findByUserIdAndBroadcastId("user_1", "bc_dismissible")).thenReturn(Optional.empty());
        when(interactionRepository.save(any(UserBroadcastInteraction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StaffBroadcastResponse response = staffBroadcastService.dismissBroadcast("bc_dismissible", "user_1");

        assertNotNull(response);
        assertTrue(response.isDismissed());
        assertEquals(FIXED_NOW, response.getDismissedAt());
    }
}
