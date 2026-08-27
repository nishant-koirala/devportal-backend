package com.fonepay.devportal.modules.admin.invitation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateInvitationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|EDITOR", message = "Role must be ADMIN or EDITOR")
    private String role;

    @NotBlank(message = "Department is required")
    private String departmentId;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;
}
