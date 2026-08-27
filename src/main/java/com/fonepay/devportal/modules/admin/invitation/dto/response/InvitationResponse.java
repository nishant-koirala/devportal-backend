package com.fonepay.devportal.modules.admin.invitation.dto.response;

import java.time.Instant;

import com.fonepay.devportal.common.constant.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {

    private String userId;
    private String email;
    private String fullName;
    private String role;
    private String departmentId;
    private String departmentName;
    private UserStatus status;
    private Instant expiresAt;
    private boolean resent;
}
