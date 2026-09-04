package com.fonepay.devportal.modules.auth.dto.request;

import java.util.List;

import com.fonepay.devportal.common.validation.ValidPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String companyName;

    @NotEmpty(message = "At least one product is required")
    private List<String> productIds;
}
