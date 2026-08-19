package com.fonepay.devportal.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    private String token;

    @NotBlank(message = "New password is required")
    @Pattern(
            regexp = "^(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{12,64}$",
            message = "Password must be between 12 and 64 characters and contain at least one digit and one symbol")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
