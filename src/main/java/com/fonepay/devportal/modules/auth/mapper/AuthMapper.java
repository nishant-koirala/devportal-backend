package com.fonepay.devportal.modules.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.modules.auth.dto.reponse.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.user.entity.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "token", source = "token")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "authStatus", source = "authStatus")
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    AuthResponse toAuthResponse(User user, String token, String message, AuthStatus authStatus);

    @Mapping(target = "message", ignore = true)
    RegistrationResponse toRegistrationResponse(User user);

    @Mapping(target = "message", source = "message")
    @Mapping(target = "expiresInSeconds", source = "expiresInSeconds")
    OtpResponse toOtpResponse(String message, int expiresInSeconds);
}
