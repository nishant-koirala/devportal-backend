package com.fonepay.devportal.modules.auth.controller;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.util.HttpRequestUtil;
import com.fonepay.devportal.modules.auth.dto.reponse.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiRoutes.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final Clock clock;

    @PostMapping(ApiRoutes.Auth.LOGIN)
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = HttpRequestUtil.getClientIp(httpRequest);
        String userAgent = HttpRequestUtil.getUserAgent(httpRequest);

        AuthResponse authResponse = authService.login(request, ipAddress, userAgent);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Login successful")
                .data(authResponse)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PostMapping(ApiRoutes.Auth.LOGOUT)
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        authService.logout(authHeader);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Logged out successfully")
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PostMapping(ApiRoutes.Auth.REGISTER)
    public ResponseEntity<ApiResponse<com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse>> register(
            @Valid @RequestBody com.fonepay.devportal.modules.auth.dto.request.RegisterRequest request) {
        
        com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("User registered successfully. Please check your email for verification.")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @org.springframework.web.bind.annotation.GetMapping(ApiRoutes.Auth.VERIFY_EMAIL)
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @org.springframework.web.bind.annotation.RequestParam("token") String token) {

        authService.verifyEmail(token);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Email verified successfully. You can now login.")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }

    @PostMapping(ApiRoutes.Auth.RESEND_VERIFICATION)
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(
            @org.springframework.web.bind.annotation.RequestParam("email") String email) {

        authService.resendVerificationEmail(email);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Verification email resent successfully.")
                        .timestamp(LocalDateTime.now(clock))
                        .build()
        );
    }
}
