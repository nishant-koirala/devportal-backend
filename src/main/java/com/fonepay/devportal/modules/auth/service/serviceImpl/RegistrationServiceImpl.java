package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fonepay.devportal.modules.auth.service.RegistrationService;

import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.modules.admin.developer.service.ActivityRecordingService;
import com.fonepay.devportal.common.exception.EmailAlreadyVerifiedException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.exception.UserAlreadyExistsException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.dto.request.RegisterRequest;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.service.UserTokenService;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.enums.ProductStatus;
import com.fonepay.devportal.modules.cms.repository.ProductRepository;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.document.UserProduct;
import com.fonepay.devportal.modules.user.repository.UserProductRepository;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserRoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private static final String DEFAULT_ROLE = "DEVELOPER";
    private static final long EMAIL_VERIFICATION_TOKEN_HOURS = 24;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final UserTokenService userTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final Clock clock;
    private final ActivityRecordingService activityRecordingService;
    private final ProductRepository productRepository;
    private final UserProductRepository userProductRepository;
    @Value("${app.frontend.url:${FRONTEND_URL:http://localhost:3000}}")
    private String frontendUrl;

    @Override
    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        List<String> publishedProductIds = resolvePublishedProductIds(request.getProductIds());

        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.isEmailVerified() || hasInternalStaffRole(existingUser)) {
                throw new UserAlreadyExistsException("User already exists with email: " + email);
            }
            
            // Overwrite existing PENDING user data
            existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            existingUser.setFullName(request.getFullName());
            existingUser.setCompanyName(request.getCompanyName());
            existingUser.setUpdatedAt(Instant.now(clock));
            
            existingUser = userRepository.save(existingUser);
            replaceUserProducts(existingUser.getUserId(), publishedProductIds);
            
            // Delete old tokens and generate a new one
            userTokenService.deleteAllTokensForUser(existingUser.getUserId());
            String rawToken = userTokenService.createAndSaveToken(
                    existingUser.getUserId(), TokenType.EMAIL_VERIFICATION, EMAIL_VERIFICATION_TOKEN_HOURS);
            
            sendVerificationEmail(existingUser.getEmail(), rawToken);
            
            return authMapper.toRegistrationResponse(existingUser);
        }
        Instant now = Instant.now(clock);
        User user = new User();
        user.setUserId(IdGenerator.nextUlid());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setCompanyName(request.getCompanyName());
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        user = userRepository.save(user);
        userRoleService.assignDefaultRole(user.getUserId(), DEFAULT_ROLE);
        replaceUserProducts(user.getUserId(), publishedProductIds);

        // The role was saved to the DB, but our in-memory user object doesn't know about it yet!
        // We must re-fetch the user to get the updated roles list before returning the response.
        user = userRepository.findById(user.getUserId()).orElse(user);

        String rawToken = userTokenService.createAndSaveToken(
                user.getUserId(), TokenType.EMAIL_VERIFICATION, EMAIL_VERIFICATION_TOKEN_HOURS);
        sendVerificationEmail(user.getEmail(), rawToken);

        return authMapper.toRegistrationResponse(user);
    }

    @Override
    public void verifyEmail(String rawToken) {
        UserToken token = userTokenService.validateAndConsumeToken(rawToken, TokenType.EMAIL_VERIFICATION);

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(Instant.now(clock));
        userRepository.save(user);
        activityRecordingService.record(user.getUserId(), ActivityType.EMAIL_VERIFIED);
    }

    @Override
    public void resendVerificationEmail(String rawEmail) {
        String email = rawEmail.trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        userTokenService.checkRateLimit(user.getUserId(), TokenType.EMAIL_VERIFICATION, RESEND_COOLDOWN_SECONDS);

        String rawToken = userTokenService.createAndSaveToken(
                user.getUserId(), TokenType.EMAIL_VERIFICATION, EMAIL_VERIFICATION_TOKEN_HOURS);
        sendVerificationEmail(user.getEmail(), rawToken);
    }

    private void sendVerificationEmail(String email, String rawToken) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + rawToken;
        emailService.sendVerificationEmail(email, verificationUrl);
    }

    private boolean hasInternalStaffRole(User user) {
        if (user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getRoleName())
                        || "EDITOR".equalsIgnoreCase(role.getRoleName()));
    }

    private List<String> resolvePublishedProductIds(List<String> productIds) {
        LinkedHashSet<String> requestedIds = productIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (requestedIds.isEmpty()) {
            throw new BadRequestException("At least one product is required");
        }

        Set<String> publishedIds = productRepository.findAllById(requestedIds).stream()
                .filter(product -> product.getStatus() == ProductStatus.PUBLISHED)
                .map(Product::getId)
                .collect(Collectors.toSet());

        List<String> resolved = requestedIds.stream()
                .filter(publishedIds::contains)
                .toList();

        if (resolved.isEmpty()) {
            throw new BadRequestException("At least one published product is required");
        }
        return resolved;
    }

    private void replaceUserProducts(String userId, List<String> productIds) {
        userProductRepository.deleteAll(userProductRepository.findByUserId(userId));
        userProductRepository.flush();
        Instant selectedAt = Instant.now(clock);
        userProductRepository.saveAll(productIds.stream()
                .map(productId -> UserProduct.builder()
                        .userId(userId)
                        .productId(productId)
                        .selectedAt(selectedAt)
                        .build())
                .toList());
    }
}

