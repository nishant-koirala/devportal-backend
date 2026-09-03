package com.fonepay.devportal.modules.admin.invitation.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.UserAlreadyExistsException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.admin.invitation.dto.request.CreateInvitationRequest;
import com.fonepay.devportal.modules.admin.invitation.dto.response.InvitationResponse;
import com.fonepay.devportal.modules.admin.invitation.service.InvitationService;
import com.fonepay.devportal.modules.auth.service.UserTokenService;
import com.fonepay.devportal.modules.department.entity.Department;
import com.fonepay.devportal.modules.department.service.DepartmentService;
import com.fonepay.devportal.modules.notification.service.EmailService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.document.UserRole;
import com.fonepay.devportal.modules.user.repository.UserRepository;
import com.fonepay.devportal.modules.user.service.UserRoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private static final long INVITE_TOKEN_HOURS = 48;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final UserTokenService userTokenService;
    private final DepartmentService departmentService;
    private final EmailService emailService;
    private final Clock clock;

    @Value("${app.frontend.url:${FRONTEND_URL:http://localhost:3000}}")
    private String frontendUrl;

    @Override
    @Transactional
    public InvitationResponse invite(CreateInvitationRequest request, String invitedByUserId) {
        String email = request.getEmail().trim().toLowerCase();
        String role = request.getRole().trim().toUpperCase();
        String fullName = trimToNull(request.getFullName());

        Department department = departmentService.requireActiveDepartment(request.getDepartmentId());

        Optional<User> existingOpt = userRepository.findByEmail(email);
        if (existingOpt.isPresent()) {
            return resendExistingInvite(existingOpt.get(), role, department, fullName, invitedByUserId);
        }

        Instant now = Instant.now(clock);
        User user = new User();
        user.setUserId(IdGenerator.nextUlid());
        user.setEmail(email);
        user.setFullName(fullName);
        user.setDepartmentId(department.getDepartmentId());
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(false);
        user.setRoles(new ArrayList<>());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user = userRepository.save(user);

        userRoleService.replaceStaffRole(user.getUserId(), role, invitedByUserId);
        user = userRepository.findById(user.getUserId()).orElse(user);

        Instant expiresAt = sendInviteEmail(user, role, department.getDepartmentName());
        log.info("Admin {} invited {} as {} in department {}", invitedByUserId, email, role,
                department.getDepartmentId());

        return toResponse(user, role, department.getDepartmentName(), expiresAt, false);
    }

    private InvitationResponse resendExistingInvite(User existing, String role, Department department,
            String fullName, String invitedByUserId) {
        if (existing.isEmailVerified() || existing.getStatus() != UserStatus.PENDING
                || !hasInternalStaffRole(existing)) {
            throw new UserAlreadyExistsException("User already exists with email: " + existing.getEmail());
        }

        if (fullName != null) {
            existing.setFullName(fullName);
        }
        existing.setDepartmentId(department.getDepartmentId());
        existing.setUpdatedAt(Instant.now(clock));
        existing = userRepository.save(existing);

        userRoleService.replaceStaffRole(existing.getUserId(), role, invitedByUserId);
        existing = userRepository.findById(existing.getUserId()).orElse(existing);

        Instant expiresAt = sendInviteEmail(existing, role, department.getDepartmentName());
        log.info("Resent staff invite to {} as {}", existing.getEmail(), role);

        return toResponse(existing, role, department.getDepartmentName(), expiresAt, true);
    }

    private Instant sendInviteEmail(User user, String role, String departmentName) {
        userTokenService.checkRateLimit(user.getUserId(), TokenType.INVITE, RESEND_COOLDOWN_SECONDS);
        String rawToken = userTokenService.createAndSaveToken(user.getUserId(), TokenType.INVITE, INVITE_TOKEN_HOURS);
        String inviteUrl = frontendUrl + "/accept-invite?token=" + rawToken;
        emailService.sendInviteEmail(user.getEmail(), inviteUrl, role, departmentName, user.getFullName());
        return Instant.now(clock).plus(INVITE_TOKEN_HOURS, ChronoUnit.HOURS);
    }

    private InvitationResponse toResponse(User user, String role, String departmentName, Instant expiresAt,
            boolean resent) {
        return InvitationResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(role)
                .departmentId(user.getDepartmentId())
                .departmentName(departmentName)
                .status(user.getStatus())
                .expiresAt(expiresAt)
                .resent(resent)
                .build();
    }

    private boolean hasInternalStaffRole(User user) {
        if (user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream()
                .map(UserRole::getRoleName)
                .anyMatch(name -> "ADMIN".equalsIgnoreCase(name) || "EDITOR".equalsIgnoreCase(name));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
