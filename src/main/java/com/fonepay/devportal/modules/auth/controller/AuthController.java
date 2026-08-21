package com.fonepay.devportal.modules.auth.controller;

import java.time.Clock;
import java.time.LocalDateTime;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.common.exception.UnauthorizedException;
import com.fonepay.devportal.common.util.HttpRequestUtil;
import com.fonepay.devportal.modules.auth.dto.request.ForgotPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.request.LoginRequest;
import com.fonepay.devportal.modules.auth.dto.request.OtpVerifyRequest;
import com.fonepay.devportal.modules.auth.dto.request.RegisterRequest;
import com.fonepay.devportal.modules.auth.dto.request.ResetPasswordRequest;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.auth.service.AdminAuthService;
import com.fonepay.devportal.modules.auth.service.AuthService;
import com.fonepay.devportal.modules.auth.service.LoginService;
import com.fonepay.devportal.modules.auth.service.RegistrationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiRoutes.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final RegistrationService registrationService;
    private final AuthService authService;
    private final AdminAuthService adminAuthService;
    private final Clock clock;

    @PostMapping(ApiRoutes.Auth.LOGIN)
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = HttpRequestUtil.getClientIp(httpRequest);
        String userAgent = HttpRequestUtil.getUserAgent(httpRequest);

        AuthResponse authResponse = loginService.login(request, ipAddress, userAgent);

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

        loginService.logout(authHeader);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Logged out successfully")
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PostMapping(ApiRoutes.Auth.REGISTER)
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        RegistrationResponse response = registrationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<RegistrationResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("User registered successfully. Please check your email for verification.")
                        .data(response)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @GetMapping(ApiRoutes.Auth.VERIFY_EMAIL)
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam("token") String token) {

        registrationService.verifyEmail(token);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Email verified successfully. You can now login.")
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Auth.RESEND_VERIFICATION)
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(@RequestParam("email") String email) {

        registrationService.resendVerificationEmail(email);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Verification email resent successfully.")
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Auth.FORGOT_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Check your email for a password reset link. If you don't see it, look in spam or try again in a minute.")
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Auth.RESET_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Password has been reset successfully. Please log in.")
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping(ApiRoutes.Auth.OTP_REQUEST)
    public ResponseEntity<ApiResponse<OtpResponse>> requestOtp(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Let GlobalExceptionHandler return the standard 401 ApiResponse.
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }

        String pendingAuthId = authHeader.substring(7);
        OtpResponse response = authService.requestOtp(pendingAuthId);

        return ResponseEntity.ok(ApiResponse.<OtpResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("OTP sent")
                .data(response)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PostMapping(ApiRoutes.Auth.OTP_VERIFY)
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Let GlobalExceptionHandler return the standard 401 ApiResponse.
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }

        String pendingAuthId = authHeader.substring(7);
        AuthResponse authResponse = authService.verifyOtp(pendingAuthId, request);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Login successful")
                .data(authResponse)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PostMapping(ApiRoutes.Auth.ADMIN_LOGIN)
    public ResponseEntity<ApiResponse<AuthResponse>> adminLogin(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = HttpRequestUtil.getClientIp(httpRequest);
        String userAgent = HttpRequestUtil.getUserAgent(httpRequest);

        AuthResponse authResponse = adminAuthService.adminLogin(request, ipAddress, userAgent);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Admin credentials verified. OTP required.")
                .data(authResponse)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PostMapping(ApiRoutes.Auth.EDITOR_LOGIN)
    public ResponseEntity<ApiResponse<AuthResponse>> editorLogin(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = HttpRequestUtil.getClientIp(httpRequest);
        String userAgent = HttpRequestUtil.getUserAgent(httpRequest);

        AuthResponse authResponse = adminAuthService.editorLogin(request, ipAddress, userAgent);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Editor credentials verified. OTP required.")
                .data(authResponse)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PostMapping(ApiRoutes.Auth.ADMIN_OTP_SETUP)
    public ResponseEntity<ApiResponse<OtpResponse>> setupAdminOtp(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }

        String pendingAuthId = authHeader.substring(7);
        OtpResponse response = adminAuthService.setupOtp(pendingAuthId);

        return ResponseEntity.ok(ApiResponse.<OtpResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("OTP sent")
                .data(response)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PostMapping(ApiRoutes.Auth.ADMIN_OTP_VERIFY)
    public ResponseEntity<ApiResponse<AuthResponse>> verifyAdminOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }

        String pendingAuthId = authHeader.substring(7);
        AuthResponse authResponse = adminAuthService.verifyAdminOtp(pendingAuthId, request);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Admin login successful")
                .data(authResponse)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }

    @PostMapping(ApiRoutes.Auth.EDITOR_OTP_VERIFY)
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEditorOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid or missing Authorization header");
        }

        String pendingAuthId = authHeader.substring(7);
        AuthResponse authResponse = adminAuthService.verifyEditorOtp(pendingAuthId, request);

        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .status(HttpStatus.OK.value())
                .success(true)
                .message("Editor login successful")
                .data(authResponse)
                .timestamp(LocalDateTime.now(clock))
                .build());
    }
}
