package com.fonepay.devportal.modules.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.common.constant.enums.PendingAuthStatus;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.InvalidOtpException;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.auth.document.PendingAuth;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.repository.PendingAuthRepository;
import com.fonepay.devportal.modules.auth.service.serviceImpl.AdminAuthServiceImpl;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.document.UserSession;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserRoleService;
import com.fonepay.devportal.modules.user.service.UserSessionService;
import com.fonepay.devportal.security.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private UserRoleService userRoleService;

        @Mock
        private UserSessionService userSessionService;

        @Mock
        private PendingAuthService pendingAuthService;

        @Mock
        private PendingAuthRepository pendingAuthRepository;

        @Mock
        private OtpService otpService;

        @Mock
        private EmailService emailService;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private JwtUtil jwtUtil;

        @Mock
        private AuthMapper authMapper;

        @Spy
        private Clock clock = Clock.fixed(Instant.parse("2026-08-21T10:00:00Z"), ZoneId.of("UTC"));

        @InjectMocks
        private AdminAuthServiceImpl adminAuthService;

        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(adminAuthService, "jwtExpirationMs", 86400000L);
                ReflectionTestUtils.setField(adminAuthService, "otpExpirationMinutes", 5);
        }

        @Test
        void adminLogin_Success_WhenUserHasAdminRole() {
                LoginRequest request = new LoginRequest("admin@fonepay.com", "password123");
                User user = new User();
                user.setUserId("user-1");
                user.setEmail("admin@fonepay.com");
                user.setPasswordHash("hashed-pw");
                user.setStatus(UserStatus.ACTIVE);

                PendingAuth pendingAuth = PendingAuth.builder()
                                .id("pending-1")
                                .userId("user-1")
                                .status(PendingAuthStatus.PENDING)
                                .build();

                when(userRepository.findByEmail("admin@fonepay.com")).thenReturn(Optional.of(user));
                when(passwordEncoder.matches("password123", "hashed-pw")).thenReturn(true);
                when(userRoleService.getRoleNamesByUserId("user-1")).thenReturn(List.of("ADMIN"));
                when(otpService.generateOtpCode()).thenReturn("123456");
                when(otpService.hashOtp("123456")).thenReturn("hashed-otp");
                when(pendingAuthService.createPendingAuth("user-1", "hashed-otp", 5)).thenReturn(pendingAuth);
                when(authMapper.toAuthResponse(user, "pending-1", AuthStatus.OTP_REQUIRED))
                                .thenReturn(AuthResponse.builder().authStatus(AuthStatus.OTP_REQUIRED)
                                                .token("pending-1").build());

                AuthResponse response = adminAuthService.adminLogin(request, "127.0.0.1", "JUnit");

                assertNotNull(response);
                assertEquals(AuthStatus.OTP_REQUIRED, response.getAuthStatus());
                verify(emailService).sendOtpEmail(eq("admin@fonepay.com"), eq("123456"), any());
        }

        @Test
        void adminLogin_ThrowsForbidden_WhenUserLacksAdminRole() {
                LoginRequest request = new LoginRequest("dev@fonepay.com", "password123");
                User user = new User();
                user.setUserId("user-2");
                user.setEmail("dev@fonepay.com");
                user.setPasswordHash("hashed-pw");
                user.setStatus(UserStatus.ACTIVE);

                when(userRepository.findByEmail("dev@fonepay.com")).thenReturn(Optional.of(user));
                when(passwordEncoder.matches("password123", "hashed-pw")).thenReturn(true);
                when(userRoleService.getRoleNamesByUserId("user-2")).thenReturn(List.of("DEVELOPER"));

                assertThrows(ForbiddenException.class,
                                () -> adminAuthService.adminLogin(request, "127.0.0.1", "JUnit"));
        }

        @Test
        void editorLogin_Success_WhenUserHasEditorRole() {
                LoginRequest request = new LoginRequest("editor@fonepay.com", "password123");
                User user = new User();
                user.setUserId("user-3");
                user.setEmail("editor@fonepay.com");
                user.setPasswordHash("hashed-pw");
                user.setStatus(UserStatus.ACTIVE);

                PendingAuth pendingAuth = PendingAuth.builder()
                                .id("pending-3")
                                .userId("user-3")
                                .status(PendingAuthStatus.PENDING)
                                .build();

                when(userRepository.findByEmail("editor@fonepay.com")).thenReturn(Optional.of(user));
                when(passwordEncoder.matches("password123", "hashed-pw")).thenReturn(true);
                when(userRoleService.getRoleNamesByUserId("user-3")).thenReturn(List.of("EDITOR"));
                when(otpService.generateOtpCode()).thenReturn("654321");
                when(otpService.hashOtp("654321")).thenReturn("hashed-otp-3");
                when(pendingAuthService.createPendingAuth("user-3", "hashed-otp-3", 5)).thenReturn(pendingAuth);
                when(authMapper.toAuthResponse(user, "pending-3", AuthStatus.OTP_REQUIRED))
                                .thenReturn(AuthResponse.builder().authStatus(AuthStatus.OTP_REQUIRED)
                                                .token("pending-3").build());

                AuthResponse response = adminAuthService.editorLogin(request, "127.0.0.1", "JUnit");

                assertNotNull(response);
                assertEquals(AuthStatus.OTP_REQUIRED, response.getAuthStatus());
        }

        @Test
        void verifyAdminOtp_Success() {
                OtpVerifyRequest request = new OtpVerifyRequest("123456");
                PendingAuth pendingAuth = PendingAuth.builder()
                                .id("pending-1")
                                .userId("user-1")
                                .status(PendingAuthStatus.PENDING)
                                .build();

                User user = new User();
                user.setUserId("user-1");

                UserSession session = UserSession.builder()
                                .sessionId("sess-1")
                                .userId("user-1")
                                .build();

                when(pendingAuthService.findById("pending-1")).thenReturn(Optional.of(pendingAuth));
                when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
                when(userRoleService.getRoleNamesByUserId("user-1")).thenReturn(List.of("ADMIN"));
                when(pendingAuthService.verifyOtp(pendingAuth, "123456", 3)).thenReturn(true);
                when(userSessionService.createSession(eq("user-1"), any(), any(), anyLong())).thenReturn(session);
                when(jwtUtil.generateToken(user, "sess-1", List.of("ADMIN"))).thenReturn("jwt-token-123");
                when(authMapper.toAuthResponse(user, "jwt-token-123", List.of("ADMIN"), AuthStatus.LOGIN_SUCCESS))
                                .thenReturn(AuthResponse.builder().token("jwt-token-123")
                                                .authStatus(AuthStatus.LOGIN_SUCCESS).build());

                AuthResponse response = adminAuthService.verifyAdminOtp("pending-1", request);

                assertNotNull(response);
                assertEquals(AuthStatus.LOGIN_SUCCESS, response.getAuthStatus());
                verify(pendingAuthService).deletePendingAuth(pendingAuth);
        }
}
