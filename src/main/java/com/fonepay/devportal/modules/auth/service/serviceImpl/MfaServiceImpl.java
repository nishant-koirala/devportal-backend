package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.common.constant.enums.OtpStatus;
import com.fonepay.devportal.common.exception.ForbiddenException;
import com.fonepay.devportal.common.exception.InvalidOtpException;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.policy.MfaPolicy;
import com.fonepay.devportal.modules.auth.service.OtpService;
import com.fonepay.devportal.modules.auth.service.TempTokenService;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserRoleService;
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
    private final MfaPolicy mfaPolicy;
    private final OtpService otpService;
    private final TempTokenService tempTokenService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;

    public OtpResponse requestOtp(String tempToken) {
        User user = validateTempTokenAndGetUser(tempToken);

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        if (!mfaPolicy.isMfaRequired(user, roleNames)) {
            throw new ForbiddenException("OTP not required for this user role");
        }

        if (otpService.hasPendingOtp(user)) {
            long remainingSeconds = otpService.getOtpRemainingSeconds(user);
            return authMapper.toOtpResponse(
                    "OTP already sent. Please check your email or wait for it to expire.",
                    (int) remainingSeconds);
        }

        String otpCode = otpService.generateOtp(user);
        userRepository.save(user);

        emailService.sendOtpEmail(user.getEmail(), otpCode, user.getFullName());
        log.info("OTP resent for user: {}", user.getUserId());

        return authMapper.toOtpResponse(
                "OTP sent to your email",
                (int) otpService.getOtpRemainingSeconds(user));
    }

    public AuthResponse verifyOtp(String tempToken, OtpVerifyRequest request) {
        if (!tempTokenService.validateTempToken(tempToken)) {
            throw new UnauthorizedException("Invalid or expired temporary token");
        }

        String userId = tempTokenService.extractUserId(tempToken);
        String sessionId = tempTokenService.extractSessionId(tempToken);

        if (userId == null || sessionId == null) {
            throw new UnauthorizedException("Invalid temporary token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        List<String> roleNames = userRoleService.getRoleNamesByUserId(user.getUserId());
        if (!mfaPolicy.isMfaRequired(user, roleNames)) {
            throw new ForbiddenException("OTP verification not required for this user");
        }

        if (!otpService.verifyOtp(user, request.getCode())) {
            userRepository.save(user);

            if (user.getOtpStatus() == OtpStatus.EXPIRED) {
                throw new InvalidOtpException("OTP has expired. Please request a new one.");
            } else if (user.getOtpStatus() == OtpStatus.FAILED) {
                throw new InvalidOtpException("Max OTP attempts exceeded. Please request a new OTP.");
            } else {
                int remainingAttempts = MAX_OTP_ATTEMPTS - user.getOtpAttempts();
                throw new InvalidOtpException("Invalid OTP code. " + remainingAttempts + " attempts remaining.");
            }
        }

        otpService.clearOtp(user);
        userRepository.save(user);
        log.info("OTP verification successful for user: {}", userId);

        String token = jwtUtil.generateToken(user, sessionId, roleNames);
        return authMapper.toAuthResponse(user, token, roleNames, AuthStatus.LOGIN_SUCCESS);
    }

    private User validateTempTokenAndGetUser(String tempToken) {
        if (!tempTokenService.validateTempToken(tempToken)) {
            throw new UnauthorizedException("Invalid or expired temporary token");
        }

        String userId = tempTokenService.extractUserId(tempToken);
        if (userId == null) {
            throw new UnauthorizedException("Invalid temporary token");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}
