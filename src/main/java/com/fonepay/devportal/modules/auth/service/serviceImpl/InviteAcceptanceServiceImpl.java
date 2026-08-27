package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.TokenType;
import com.fonepay.devportal.common.constant.enums.UserStatus;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.InvalidOrExpiredTokenException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.auth.document.UserToken;
import com.fonepay.devportal.modules.auth.dto.request.AcceptInviteRequest;
import com.fonepay.devportal.modules.auth.dto.response.InvitePreviewResponse;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.auth.mapper.AuthMapper;
import com.fonepay.devportal.modules.auth.service.InviteAcceptanceService;
import com.fonepay.devportal.modules.auth.service.UserTokenService;
import com.fonepay.devportal.modules.department.entity.Department;
import com.fonepay.devportal.modules.department.repository.DepartmentRepository;
import com.fonepay.devportal.modules.user.document.AssignedRole;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteAcceptanceServiceImpl implements InviteAcceptanceService {

    private final UserTokenService userTokenService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final Clock clock;

    @Override
    public InvitePreviewResponse previewInvite(String token) {
        User user = loadPendingInvitedUser(userTokenService.validateToken(token, TokenType.INVITE));
        return InvitePreviewResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(staffRoleOf(user))
                .departmentId(user.getDepartmentId())
                .departmentName(departmentNameOf(user.getDepartmentId()))
                .build();
    }

    @Override
    public RegistrationResponse acceptInvite(AcceptInviteRequest request) {
        UserToken token = userTokenService.validateToken(request.getToken(), TokenType.INVITE);
        User user = loadPendingInvitedUser(token);

        Instant now = Instant.now(clock);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(now);
        user = userRepository.save(user);

        userTokenService.consumeToken(token);
        log.info("Invite accepted for user {}", user.getUserId());

        return authMapper.toRegistrationResponse(user);
    }

    private User loadPendingInvitedUser(UserToken token) {
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.PENDING || user.isEmailVerified()) {
            throw new InvalidOrExpiredTokenException("This invitation has already been used.");
        }
        if (staffRoleOf(user) == null) {
            throw new BadRequestException("This invitation is not valid for staff registration");
        }
        return user;
    }

    private String staffRoleOf(User user) {
        if (user.getRoles() == null) {
            return null;
        }
        return user.getRoles().stream()
                .filter(role -> "ADMIN".equalsIgnoreCase(role.getRoleName())
                        || "EDITOR".equalsIgnoreCase(role.getRoleName()))
                .max(java.util.Comparator.comparing(
                        AssignedRole::getAssignedAt,
                        java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())))
                .map(AssignedRole::getRoleName)
                .orElse(null);
    }

    private String departmentNameOf(String departmentId) {
        if (departmentId == null || departmentId.isBlank()) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .map(Department::getDepartmentName)
                .orElse(null);
    }
}
