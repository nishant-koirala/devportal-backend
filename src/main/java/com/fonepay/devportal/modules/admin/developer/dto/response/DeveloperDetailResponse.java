package com.fonepay.devportal.modules.admin.developer.dto.response;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fonepay.devportal.common.constant.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeveloperDetailResponse {

    private String userId;
    private String email;
    private String fullName;
    private String companyName;
    private UserStatus status;
    private boolean emailVerified;
    private Instant lastLoginAt;
    private String departmentId;
    private Instant deactivatedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> roles;
}
