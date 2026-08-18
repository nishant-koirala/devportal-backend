package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.EmailAlreadyVerifiedException;
import com.fonepay.devportal.common.exception.InvalidOrExpiredTokenException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.exception.TooManyRequestsException;
import com.fonepay.devportal.common.exception.UserAlreadyExistsException;
import com.fonepay.devportal.modules.auth.document.VerificationToken;
import com.fonepay.devportal.modules.auth.dto.request.RegisterRequest;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.repository.VerificationTokenRepository;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.entity.User;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final EmailService emailService;
    private final Clock clock;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Override
    public RegistrationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setCompanyName(request.getCompanyName());
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(false);
        user.setCreatedAt(Instant.now(clock));
        user.setUpdatedAt(Instant.now(clock));

        user = userRepository.save(user);

        VerificationToken token = generateVerificationToken(user);
        sendVerificationEmail(user.getEmail(), token.getToken());

        return authMapper.toRegistrationResponse(user);
    }

    @Override
    public void verifyEmail(String tokenStr) {
        VerificationToken token = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Invalid verification token"));

        if (token.getExpiryDate().isBefore(Instant.now(clock))) {
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

        tokenRepository.delete(token);
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        Optional<VerificationToken> existingTokenOpt = tokenRepository.findByUserId(user.getUserId());
        if (existingTokenOpt.isPresent()) {
            VerificationToken existingToken = existingTokenOpt.get();
            long secondsSinceCreation = ChronoUnit.SECONDS.between(existingToken.getCreatedAt(), Instant.now(clock));
            if (secondsSinceCreation < 60) {
                throw new TooManyRequestsException("Please wait 60 seconds before requesting a new token");
            }
            tokenRepository.delete(existingToken);
        }

        VerificationToken newToken = generateVerificationToken(user);
        sendVerificationEmail(user.getEmail(), newToken.getToken());
    }

    private VerificationToken generateVerificationToken(User user) {
        Instant now = Instant.now(clock);
        VerificationToken token = VerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .userId(user.getUserId())
                .createdAt(now)
                .expiryDate(now.plus(24, ChronoUnit.HOURS))
                .build();

        return tokenRepository.save(token);
    }

    private void sendVerificationEmail(String email, String token) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        emailService.sendVerificationEmail(email, verificationUrl);
    }
}
