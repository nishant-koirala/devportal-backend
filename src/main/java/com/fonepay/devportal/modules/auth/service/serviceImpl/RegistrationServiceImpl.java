package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.modules.auth.service.RegistrationService;

import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.EmailAlreadyVerifiedException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.exception.UserAlreadyExistsException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.dto.request.RegisterRequest;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.service.UserTokenService;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserRoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private static final String DEFAULT_ROLE = "ADMIN";
    private static final long EMAIL_VERIFICATION_TOKEN_HOURS = 24;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final UserTokenService userTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final Clock clock;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Override
    public RegistrationResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User already exists with email: " + email);
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
}
