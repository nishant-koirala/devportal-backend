package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.InvalidOtpException;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.policy.MfaPolicy;
import com.fonepay.devportal.modules.auth.repository.UserTokenRepository;
import com.fonepay.devportal.modules.auth.service.OtpService;
import com.fonepay.devportal.modules.auth.service.UserTokenService;
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
    private final UserTokenService userTokenService;
    private final UserTokenRepository userTokenRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final Clock clock;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    public OtpResponse requestOtp(String tokenId) {
        UserToken token = userTokenRepository.findByIdAndTokenType(tokenId, TokenType.LOGIN_OTP)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired login session"));

        if (token.getUsedAt() != null) {
            throw new UnauthorizedException("Invalid or expired login session");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        if (!mfaPolicy.isMfaRequired(user, roleNames)) {
            throw new ForbiddenException("OTP not required for this user role");
        }

        Instant now = clock.instant();
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(now)) {
            userTokenService.deleteToken(token);
            throw new InvalidOtpException("OTP has expired. Please request a new one by logging in again.");
        }

        // Generate new OTP
        String otpCode = otpService.generateOtpCode();
        String otpHash = userTokenService.hashToken(otpCode);

        // Update token with new OTP hash and reset attempts
        token.setTokenHash(otpHash);
        token.setAttempts(0);
        token.setExpiresAt(now.plusSeconds(otpExpirationMinutes * 60L));
        token.setUsedAt(null);
        userTokenRepository.save(token);

        emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());
        log.info("OTP resent for login token: {}", tokenId);

        return authMapper.toOtpResponse(
                "OTP sent to your email",
                (int) java.time.Duration.between(now, token.getExpiresAt()).getSeconds());
    }

    public AuthResponse verifyOtp(String tokenId, OtpVerifyRequest request) {
        UserToken token = userTokenRepository.findByIdAndTokenType(tokenId, TokenType.LOGIN_OTP)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired login session"));

        if (token.getUsedAt() != null) {
            throw new UnauthorizedException("Invalid or expired login session");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        if (!mfaPolicy.isMfaRequired(user, roleNames)) {
            throw new ForbiddenException("OTP verification not required for this user");
        }

        Instant now = clock.instant();
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(now)) {
            userTokenService.deleteToken(token);
            throw new InvalidOtpException("OTP has expired. Please request a new one by logging in again.");
        }

        boolean verified = userTokenService.verifyLoginOtp(token, request.getCode(), MAX_OTP_ATTEMPTS);

        if (!verified) {
            if (token.getAttempts() >= MAX_OTP_ATTEMPTS) {
                userTokenService.deleteToken(token);
                throw new InvalidOtpException("Max OTP attempts exceeded. Please request a new OTP by logging in again.");
            } else {
                int remainingAttempts = MAX_OTP_ATTEMPTS - token.getAttempts();
                throw new InvalidOtpException("Invalid OTP code. " + remainingAttempts + " attempts remaining.");
            }
        }

        // OTP verified successfully - delete token and create session
        userTokenService.deleteToken(token);
        log.info("OTP verification successful for user: {}", user.getUserId());

        // Create ACTIVE session now
        UserSession session = userSessionService.createSession(user.getUserId(), null, null, roleNames);
        java.util.Set<String> permissions = userRoleService.getPermissionsByUserId(user.getUserId());
        String jwt = jwtUtil.generateToken(user, session.getSessionId(), roleNames, permissions,
                session.getMaxExpiresAt());
        return authMapper.toAuthResponse(user, jwt, roleNames, AuthStatus.LOGIN_SUCCESS);
    }
}
