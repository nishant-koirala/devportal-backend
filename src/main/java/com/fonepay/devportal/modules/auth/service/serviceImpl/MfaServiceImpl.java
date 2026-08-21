package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.common.constant.enums.PendingAuthStatus;
import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.InvalidOtpException;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.auth.document.PendingAuth;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.policy.MfaPolicy;
import com.fonepay.devportal.modules.auth.repository.PendingAuthRepository;
import com.fonepay.devportal.modules.auth.service.OtpService;
import com.fonepay.devportal.modules.auth.service.PendingAuthService;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.document.UserSession;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserRoleService;
import com.fonepay.devportal.modules.user.service.UserSessionService;
import com.fonepay.devportal.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MfaServiceImpl {

    private static final int MAX_OTP_ATTEMPTS = 3;

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final UserSessionService userSessionService;
    private final MfaPolicy mfaPolicy;
    private final OtpService otpService;
    private final PendingAuthService pendingAuthService;
    private final PendingAuthRepository pendingAuthRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final Clock clock;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    public OtpResponse requestOtp(String pendingAuthId) {
        PendingAuth pendingAuth = pendingAuthService.findById(pendingAuthId)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired pending authentication"));

        if (pendingAuth.getStatus() != PendingAuthStatus.PENDING) {
            throw new UnauthorizedException("Invalid or expired pending authentication");
        }

        User user = userRepository.findById(pendingAuth.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        if (!mfaPolicy.isMfaRequired(user, roleNames)) {
            throw new ForbiddenException("OTP not required for this user role");
        }

        Instant now = clock.instant();
        if (pendingAuth.getExpiresAt() != null && pendingAuth.getExpiresAt().isBefore(now)) {
            pendingAuth.setStatus(PendingAuthStatus.EXPIRED);
            pendingAuthService.deletePendingAuth(pendingAuth);
            throw new InvalidOtpException("OTP has expired. Please request a new one by logging in again.");
        }

        // Generate new OTP
        String otpCode = otpService.generateOtpCode();
        String otpHash = otpService.hashOtp(otpCode);

        // Update pending auth with new OTP hash and reset attempts
        pendingAuth.setOtpHash(otpHash);
        pendingAuth.setAttempts(0);
        pendingAuth.setExpiresAt(now.plusSeconds(otpExpirationMinutes * 60L));
        pendingAuth.setStatus(PendingAuthStatus.PENDING);
        pendingAuth.setVerifiedAt(null);
        pendingAuthRepository.save(pendingAuth);

        emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());
        log.info("OTP resent for pending auth: {}", pendingAuthId);

        return authMapper.toOtpResponse(
                "OTP sent to your email",
                (int) java.time.Duration.between(now, pendingAuth.getExpiresAt()).getSeconds());
    }

    public AuthResponse verifyOtp(String pendingAuthId, OtpVerifyRequest request) {
        PendingAuth pendingAuth = pendingAuthService.findById(pendingAuthId)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired pending authentication"));

        if (pendingAuth.getStatus() != PendingAuthStatus.PENDING) {
            throw new UnauthorizedException("Invalid or expired pending authentication");
        }

        User user = userRepository.findById(pendingAuth.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        if (!mfaPolicy.isMfaRequired(user, roleNames)) {
            throw new ForbiddenException("OTP verification not required for this user");
        }

        boolean verified = pendingAuthService.verifyOtp(pendingAuth, request.getCode(), MAX_OTP_ATTEMPTS);

        if (!verified) {
            if (pendingAuth.getStatus() == PendingAuthStatus.EXPIRED) {
                pendingAuthService.deletePendingAuth(pendingAuth);
                throw new InvalidOtpException("OTP has expired. Please request a new one by logging in again.");
            } else if (pendingAuth.getStatus() == PendingAuthStatus.FAILED) {
                pendingAuthService.deletePendingAuth(pendingAuth);
                throw new InvalidOtpException("Max OTP attempts exceeded. Please request a new OTP by logging in again.");
            } else {
                int remainingAttempts = MAX_OTP_ATTEMPTS - pendingAuth.getAttempts();
                throw new InvalidOtpException("Invalid OTP code. " + remainingAttempts + " attempts remaining.");
            }
        }

        // OTP verified successfully - delete pending auth record and create session
        pendingAuthService.deletePendingAuth(pendingAuth);
        log.info("OTP verification successful for user: {}", user.getUserId());

        // Create ACTIVE session now
        UserSession session = userSessionService.createSession(user.getUserId(), null, null, jwtExpirationMs);
        String token = jwtUtil.generateToken(user, session.getSessionId(), roleNames);
        return authMapper.toAuthResponse(user, token, roleNames, AuthStatus.LOGIN_SUCCESS);
    }
}
