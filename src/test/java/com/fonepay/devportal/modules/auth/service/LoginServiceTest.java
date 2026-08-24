package com.fonepay.devportal.modules.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.admin.developer.activity.service.ActivityRecordingService;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.service.serviceImpl.LoginServiceImpl;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ActivityRecordingService activityRecordingService;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-08-21T10:00:00Z"), ZoneId.of("UTC"));

    @InjectMocks
    private LoginServiceImpl loginService;

    @Test
    void login_ThrowsUnauthorized_WhenUserIsInactive() {
        LoginRequest request = new LoginRequest("dev@fonepay.com", "password123");
        User user = new User();
        user.setUserId("dev-1");
        user.setEmail("dev@fonepay.com");
        user.setPasswordHash("hashed-pw");
        user.setStatus(UserStatus.INACTIVE);

        when(userRepository.findByEmail("dev@fonepay.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-pw")).thenReturn(true);

        assertThrows(UnauthorizedException.class, () -> loginService.login(request, "127.0.0.1", "JUnit"));
    }

    @Test
    void login_ThrowsUnauthorized_WhenUserIsDeactivated() {
        LoginRequest request = new LoginRequest("dev@fonepay.com", "password123");
        User user = new User();
        user.setUserId("dev-1");
        user.setEmail("dev@fonepay.com");
        user.setPasswordHash("hashed-pw");
        user.setStatus(UserStatus.DEACTIVATED);

        when(userRepository.findByEmail("dev@fonepay.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-pw")).thenReturn(true);

        assertThrows(UnauthorizedException.class, () -> loginService.login(request, "127.0.0.1", "JUnit"));
    }
}
