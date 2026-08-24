package com.fonepay.devportal.modules.admin.developer.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.admin.developer.dto.request.UpdateDeveloperStatusRequest;
import com.fonepay.devportal.modules.admin.developer.dto.response.DeveloperDetailResponse;
import com.fonepay.devportal.modules.admin.developer.service.impl.DeveloperAdminServiceImpl;
import com.fonepay.devportal.modules.user.document.AssignedRole;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserSessionService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeveloperAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSessionService userSessionService;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-08-21T10:00:00Z"), ZoneId.of("UTC"));

    @InjectMocks
    private DeveloperAdminServiceImpl developerAdminService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId("dev-123");
        sampleUser.setEmail("developer@fonepay.com");
        sampleUser.setFullName("John Developer");
        sampleUser.setCompanyName("Fonepay Corp");
        sampleUser.setStatus(UserStatus.ACTIVE);
        sampleUser.setEmailVerified(true);
        sampleUser.setRoles(new ArrayList<>(List.of(
                AssignedRole.builder().roleName("DEVELOPER").build()
        )));
    }

    @Test
    void getDeveloperById_Success() {
        when(userRepository.findById("dev-123")).thenReturn(Optional.of(sampleUser));

        DeveloperDetailResponse response = developerAdminService.getDeveloperById("dev-123");

        assertNotNull(response);
        assertEquals("dev-123", response.getUserId());
        assertEquals("developer@fonepay.com", response.getEmail());
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertTrue(response.getRoles().contains("DEVELOPER"));
    }

    @Test
    void getDeveloperById_NotFound_ThrowsException() {
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> developerAdminService.getDeveloperById("non-existent"));
    }

    @Test
    void updateDeveloperStatus_Deactivate_Success_RevokesSessions() {
        sampleUser.setStatus(UserStatus.ACTIVE);
        UpdateDeveloperStatusRequest request = new UpdateDeveloperStatusRequest(UserStatus.INACTIVE);

        when(userRepository.findById("dev-123")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeveloperDetailResponse response = developerAdminService.updateDeveloperStatus("dev-123", request);

        assertNotNull(response);
        assertEquals(UserStatus.INACTIVE, response.getStatus());
        assertNotNull(response.getDeactivatedAt());
        verify(userSessionService).revokeAllActiveSessions("dev-123");
        verify(userRepository).save(sampleUser);
    }

    @Test
    void updateDeveloperStatus_Activate_Success_ClearsDeactivatedAt() {
        sampleUser.setStatus(UserStatus.INACTIVE);
        sampleUser.setDeactivatedAt(Instant.parse("2026-08-20T10:00:00Z"));
        UpdateDeveloperStatusRequest request = new UpdateDeveloperStatusRequest(UserStatus.ACTIVE);

        when(userRepository.findById("dev-123")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeveloperDetailResponse response = developerAdminService.updateDeveloperStatus("dev-123", request);

        assertNotNull(response);
        assertEquals(UserStatus.ACTIVE, response.getStatus());
        assertNull(response.getDeactivatedAt());
        verify(userRepository).save(sampleUser);
        verifyNoInteractions(userSessionService);
    }

    @Test
    void updateDeveloperStatus_AlreadySameStatus_ThrowsBadRequestException() {
        sampleUser.setStatus(UserStatus.ACTIVE);
        UpdateDeveloperStatusRequest request = new UpdateDeveloperStatusRequest(UserStatus.ACTIVE);

        when(userRepository.findById("dev-123")).thenReturn(Optional.of(sampleUser));

        assertThrows(BadRequestException.class, () -> developerAdminService.updateDeveloperStatus("dev-123", request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateDeveloperStatus_InvalidTransition_ThrowsBadRequestException() {
        UpdateDeveloperStatusRequest request = new UpdateDeveloperStatusRequest(UserStatus.PENDING);

        assertThrows(BadRequestException.class, () -> developerAdminService.updateDeveloperStatus("dev-123", request));
    }
}
