package com.fonepay.devportal.modules.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fonepay.devportal.modules.auth.dto.reponse.AuthResponse;
import com.fonepay.devportal.modules.user.entity.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "token", source = "token")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "status", expression = "java(user.getStatus() != null ? user.getStatus().name() : null)")
    @Mapping(target = "role", ignore = true)
    AuthResponse toAuthResponse(User user, String token, String message);
}
