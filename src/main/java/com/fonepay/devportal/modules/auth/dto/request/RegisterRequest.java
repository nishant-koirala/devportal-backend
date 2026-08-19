package com.fonepay.devportal.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{12,64}$", 
             message = "Password must be between 12 and 64 characters and contain at least one digit and one symbol")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String companyName;
}
