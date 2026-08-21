package com.fonepay.devportal.modules.admin.developer.dto.response;

import java.time.Instant;
import java.util.List;

import com.fonepay.devportal.common.constant.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperResponseDto {

    private String userId;
    private String fullName;
    private String email;
    private String companyName;
    private Instant registrationDate;
    private Instant lastLogin;
    private UserStatus accountStatus;
    private boolean emailVerified;
    private List<String> roles;
    private String departmentId;
}
