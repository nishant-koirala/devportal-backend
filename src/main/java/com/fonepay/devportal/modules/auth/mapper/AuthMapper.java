package com.fonepay.devportal.modules.auth.mapper;

import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public RegistrationResponse toRegistrationResponse(User user) {
        if (user == null) {
            return null;
        }

        return RegistrationResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .message("Registration successful. Please check your email to verify your account.")
                .build();
    }
}
