package com.fonepay.devportal.modules.user.dto.response;

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
public class UserProfileResponse {

    private String userId;
    private String email;
    private String fullName;
    private String companyName;
    private UserStatus status;
    private boolean emailVerified;
    private List<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
