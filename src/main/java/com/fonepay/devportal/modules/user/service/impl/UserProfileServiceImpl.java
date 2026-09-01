package com.fonepay.devportal.modules.user.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.service.UserTokenService;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.dto.response.PublicProductResponseDto;
import com.fonepay.devportal.modules.cms.mapper.PublicContentMapper;
import com.fonepay.devportal.modules.cms.repository.ProductRepository;
import com.fonepay.devportal.modules.developer.dto.response.UserBookmarkResponse;
import com.fonepay.devportal.modules.developer.service.UserBookmarkService;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.document.AssignedRole;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.dto.request.EmailChangeRequest;
import com.fonepay.devportal.modules.user.dto.request.UpdatePasswordRequest;
import com.fonepay.devportal.modules.user.dto.request.UpdateProfileRequest;
import com.fonepay.devportal.modules.user.dto.response.DeveloperDashboardResponse;
import com.fonepay.devportal.modules.user.dto.response.UserProfileResponse;
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
    private final ProductRepository productRepository;
    private final PublicContentMapper publicContentMapper;
    private final UserBookmarkService userBookmarkService;
    private final Clock clock;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public UserProfileResponse getProfile(String userId) {
        User user = getUser(userId);

        List<String> roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(AssignedRole::getRoleName).toList()
                : Collections.emptyList();

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .companyName(user.getCompanyName())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    public DeveloperDashboardResponse getDashboard(String userId) {
        UserProfileResponse profile = getProfile(userId);
        User user = getUser(userId);

        List<PublicProductResponseDto> subscribedProducts = Collections.emptyList();
        List<String> subscribedProductIds = user.getSubscribedProductIds();
        if (subscribedProductIds != null && !subscribedProductIds.isEmpty()) {
            List<Product> products = productRepository.findAllById(subscribedProductIds);
            subscribedProducts = publicContentMapper.toPublicProductResponseDtoList(products);
        }

        List<UserBookmarkResponse> bookmarks = userBookmarkService.getUserBookmarks(userId);

        return DeveloperDashboardResponse.builder()
                .profile(profile)
                .subscribedProducts(subscribedProducts != null ? subscribedProducts : Collections.emptyList())
                .bookmarks(bookmarks != null ? bookmarks : Collections.emptyList())
                .build();
    }

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
