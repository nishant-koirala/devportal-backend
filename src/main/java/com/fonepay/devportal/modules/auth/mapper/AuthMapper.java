package com.fonepay.devportal.modules.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.user.document.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    default AuthResponse toAuthResponse(User user, String tempToken, String message, AuthStatus authStatus) {
        return toAuthResponse(user, tempToken, null, message, authStatus);
    }

    @Mapping(target = "token", source = "token")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "authStatus", source = "authStatus")
    AuthResponse toAuthResponse(User user, String token, java.util.List<String> roles, String message, AuthStatus authStatus);

    @Mapping(target = "message", ignore = true)
    RegistrationResponse toRegistrationResponse(User user);

    @Mapping(target = "message", source = "message")
    @Mapping(target = "expiresInSeconds", source = "expiresInSeconds")
    OtpResponse toOtpResponse(String message, int expiresInSeconds);
}
