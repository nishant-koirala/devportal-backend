package com.fonepay.devportal.modules.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fonepay.devportal.common.constant.enums.OtpStatus;
import com.fonepay.devportal.common.constant.enums.UserStatus;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String userId;

    @Indexed(unique = true)
    @Field("email")
    private String email;

    @Field("password_hash")
    private String passwordHash;

    @Field("full_name")
    private String fullName;

    @Field("company_name")
    private String companyName;

    @Field("status")
    private UserStatus status;

    @Field("email_verified")
    private boolean emailVerified;

    @Field("last_login_at")
    private Instant lastLoginAt;

    @Field("department_id")
    private String departmentId;

    @Field("deactivated_at")
    private Instant deactivatedAt;


    @Field("otp_code")
    private String otpCode;

    @Field("otp_expires_at")
    private Instant otpExpiresAt;

    @Field("otp_attempts")
    private int otpAttempts;

    @Field("otp_status")
    private OtpStatus otpStatus;

    @Field("otp_verified_at")
    private Instant otpVerifiedAt;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;

}
