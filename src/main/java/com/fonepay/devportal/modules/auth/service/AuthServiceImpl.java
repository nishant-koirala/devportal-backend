package com.fonepay.devportal.modules.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.SessionStatus;
import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.EmailAlreadyVerifiedException;
import com.fonepay.devportal.common.exception.InvalidOrExpiredTokenException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.exception.TooManyRequestsException;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.common.exception.UserAlreadyExistsException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.dto.reponse.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.request.RegisterRequest;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.repository.UserTokenRepository;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.entity.User;
import com.fonepay.devportal.modules.user.entity.UserSession;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.repository.UserSessionRepository;
import com.fonepay.devportal.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final EmailService emailService;
    private final Clock clock;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Override
    public RegistrationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        User user = new User();
        user.setUserId(IdGenerator.nextUlid()); // ULID Migration
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setCompanyName(request.getCompanyName());
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(false);
        user.setCreatedAt(Instant.now(clock));
        user.setUpdatedAt(Instant.now(clock));

        user = userRepository.save(user);

        String rawToken = createAndSaveVerificationToken(user);
        sendVerificationEmail(user.getEmail(), rawToken);

        return authMapper.toRegistrationResponse(user);
    }

    @Override
    public void verifyEmail(String rawToken) {
        String hashedToken = hashToken(rawToken);

        UserToken token = tokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid verification token"));

        if (token.getUsedAt() != null) {
            throw new InvalidOrExpiredTokenException("Verification token has already been used");
        }

        if (token.getExpiresAt().isBefore(Instant.now(clock))) {
            throw new InvalidOrExpiredTokenException("Verification token has expired");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(Instant.now(clock));
        userRepository.save(user);

        // Keep the token record and mark as used
        token.setUsedAt(Instant.now(clock));
        tokenRepository.save(token);
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        Optional<UserToken> existingTokenOpt = tokenRepository.findByUserIdAndTokenTypeAndUsedAtIsNull(user.getUserId(), TokenType.EMAIL_VERIFICATION);
        
        if (existingTokenOpt.isPresent()) {
            UserToken existingToken = existingTokenOpt.get();
            long secondsSinceCreation = ChronoUnit.SECONDS.between(existingToken.getCreatedAt(), Instant.now(clock));
            if (secondsSinceCreation < 60) {
                throw new TooManyRequestsException("Please wait 60 seconds before requesting a new token");
            }
            // Hard delete the old unused token as per fixed rule
            tokenRepository.delete(existingToken);
        }

        String rawToken = createAndSaveVerificationToken(user);
        sendVerificationEmail(user.getEmail(), rawToken);
    }

    // Since I need the raw token to send in the email, I'll adjust generateVerificationToken to return the raw token.
    private String createAndSaveVerificationToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hashToken(rawToken);
        Instant now = Instant.now(clock);

        UserToken token = UserToken.builder()
                .id(IdGenerator.nextUlid())
                .userId(user.getUserId())
                .tokenHash(hashedToken)
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .createdAt(now)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .build();

        tokenRepository.save(token);
        return rawToken;
    }

    // Helper method to hash token
    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    private void sendVerificationEmail(String email, String rawToken) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + rawToken;
        emailService.sendVerificationEmail(email, verificationUrl);
    }

    @Override
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Processing login request for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Password mismatch for email: {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.DEACTIVATED) {
            log.warn("Login attempt for deactivated user: {}", user.getUserId());
            throw new UnauthorizedException("Account is deactivated. Please contact support.");
        }

        Instant now = clock.instant();

        if (ipAddress != null && userAgent != null) {
            java.util.List<UserSession> existingSameDeviceSessions = userSessionRepository
                    .findByUserIdAndIpAddressAndUserAgentAndStatus(user.getUserId(), ipAddress, userAgent, SessionStatus.ACTIVE);
            if (!existingSameDeviceSessions.isEmpty()) {
                existingSameDeviceSessions.forEach(s -> {
                    s.setStatus(SessionStatus.REVOKED);
                    s.setRevokedAt(now);
                });
                userSessionRepository.saveAll(existingSameDeviceSessions);
                log.info("Revoked {} existing active session(s) from same device (IP: {}) for user: {}",
                        existingSameDeviceSessions.size(), ipAddress, user.getUserId());
            }
        }

        String sessionId = IdGenerator.nextUlid();
        Instant expiresAt = now.plusMillis(jwtExpirationMs);

        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(user.getUserId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(now)
                .lastActivityAt(now)
                .expiresAt(expiresAt)
                .status(SessionStatus.ACTIVE)
                .build();

        userSessionRepository.save(session);
        log.info("Active session created with ID: {} for user: {}", sessionId, user.getUserId());

        user.setLastLoginAt(now);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user, sessionId);

        return authMapper.toAuthResponse(user, token, "Login successful");
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }

        String token = authHeader.substring(7);
        String sessionId = jwtUtil.extractSessionId(token);

        if (sessionId != null) {
            userSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
                session.setStatus(SessionStatus.REVOKED);
                session.setRevokedAt(clock.instant());
                userSessionRepository.save(session);
                log.info("User session revoked for sessionId: {}", sessionId);
            });
        }
    }
}
