package com.fonepay.devportal.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AcceptInviteRequest {

    @NotBlank(message = "Invite token is required")
    private String token;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{12,64}$",
            message = "Password must be between 12 and 64 characters and contain at least one digit and one symbol")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;
}
