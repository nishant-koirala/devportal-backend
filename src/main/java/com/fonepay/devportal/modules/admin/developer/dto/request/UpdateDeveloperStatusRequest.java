package com.fonepay.devportal.modules.admin.developer.dto.request;

import com.fonepay.devportal.common.constant.enums.UserStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeveloperStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
}
