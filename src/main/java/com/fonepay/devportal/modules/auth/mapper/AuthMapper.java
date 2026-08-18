package com.fonepay.devportal.modules.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fonepay.devportal.modules.auth.dto.reponse.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.user.entity.User;
import com.fonepay.devportal.common.constant.enums.AuthStatus;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "token", source = "token")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "authStatus", source = "authStatus")
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    AuthResponse toAuthResponse(User user, String token, String message, AuthStatus authStatus);

    @Mapping(target = "message", source = "message")
    @Mapping(target = "expiresInSeconds", source = "expiresInSeconds")
    OtpResponse toOtpResponse(String message, int expiresInSeconds);
}
