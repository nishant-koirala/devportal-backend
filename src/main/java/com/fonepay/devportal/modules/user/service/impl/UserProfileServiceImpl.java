package com.fonepay.devportal.modules.user.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.service.UserTokenService;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.dto.request.EmailChangeRequest;
import com.fonepay.devportal.modules.user.dto.request.UpdatePasswordRequest;
import com.fonepay.devportal.modules.user.dto.request.UpdateProfileRequest;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.repository.UserSessionRepository;
import com.fonepay.devportal.modules.user.service.UserProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserTokenService userTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void updateProfile(String userId, UpdateProfileRequest request) {
        User user = getUser(userId);
        
        user.setFullName(request.getFullName().trim());
        if (request.getCompanyName() != null) {
            user.setCompanyName(request.getCompanyName().trim());
        }
        user.setUpdatedAt(Instant.now(clock));
        
        userRepository.save(user);
        log.info("User {} updated profile.", userId);
    }

    @Override
    public void updatePassword(String userId, UpdatePasswordRequest request) {
        User user = getUser(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Instant.now(clock));
        userRepository.save(user);
        
        // Invalidate all active sessions
        userSessionRepository.deleteAllByUserId(userId);
        
        log.info("User {} updated password and all active sessions were invalidated.", userId);
    }

    @Override
    public void requestEmailChange(String userId, EmailChangeRequest request) {
        User user = getUser(userId);
        String newEmail = request.getNewEmail().trim().toLowerCase();

        if (user.getEmail().equals(newEmail)) {
            throw new BadRequestException("This is already your current email address");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new BadRequestException("This email address is already in use by another account");
        }

        user.setPendingEmail(newEmail);
        user.setUpdatedAt(Instant.now(clock));
        userRepository.save(user);

        // Generate verification token
        String rawToken = userTokenService.createAndSaveToken(userId, TokenType.EMAIL_CHANGE, 1); // 1 hour expiry
        
        String verificationUrl = frontendUrl + "/profile/email-verify?token=" + rawToken;
        emailService.sendEmailChangeVerification(newEmail, verificationUrl);
        
        log.info("User {} requested email change to {}", userId, newEmail);
    }

    @Override
    public void verifyEmailChange(String token) {
        UserToken userToken = userTokenService.validateToken(token, TokenType.EMAIL_CHANGE);
        
        User user = getUser(userToken.getUserId());
        
        if (user.getPendingEmail() == null) {
            throw new BadRequestException("No pending email change request found");
        }
        
        if (userRepository.existsByEmail(user.getPendingEmail())) {
            user.setPendingEmail(null);
            userRepository.save(user);
            throw new BadRequestException("The requested email is now in use by another account. Request cancelled.");
        }
        
        String oldEmail = user.getEmail();
        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setUpdatedAt(Instant.now(clock));
        
        userRepository.save(user);
        userTokenService.consumeToken(userToken);
        
        log.info("User {} successfully changed email from {} to {}", user.getUserId(), oldEmail, user.getEmail());
    }

    private User getUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
