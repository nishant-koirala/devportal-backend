package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.dto.request.ForgotPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.request.ResetPasswordRequest;
import com.fonepay.devportal.modules.auth.service.PasswordService;
import com.fonepay.devportal.modules.auth.service.UserTokenService;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserSessionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

    private static final long PASSWORD_RESET_TOKEN_HOURS = 1;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private final UserRepository userRepository;
    private final UserTokenService userTokenService;
    private final UserSessionService userSessionService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getStatus() == UserStatus.DEACTIVATED) {
                return;
            }

            userTokenService.checkRateLimit(user.getUserId(), TokenType.PASSWORD_RESET, RESEND_COOLDOWN_SECONDS);

            String rawToken = userTokenService.createAndSaveToken(
                    user.getUserId(), TokenType.PASSWORD_RESET, PASSWORD_RESET_TOKEN_HOURS);
            String resetUrl = frontendUrl + ApiRoutes.Auth.RESET_PASSWORD + "?token=" + rawToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        UserToken token = userTokenService.validateAndConsumeToken(request.getToken(), TokenType.PASSWORD_RESET);

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Instant now = Instant.now(clock);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(now);
        userRepository.save(user);

        userSessionService.revokeAllActiveSessions(user.getUserId());
    }
}
