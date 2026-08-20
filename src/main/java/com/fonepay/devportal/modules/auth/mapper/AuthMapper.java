package com.fonepay.devportal.modules.auth.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fonepay.devportal.common.constant.enums.AuthStatus;
import com.fonepay.devportal.modules.auth.dto.response.AuthResponse;
import com.fonepay.devportal.modules.auth.dto.response.OtpResponse;
import com.fonepay.devportal.modules.auth.dto.response.RegistrationResponse;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.document.AssignedRole;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    default AuthResponse toAuthResponse(User user, String tempToken, AuthStatus authStatus) {
        return toAuthResponse(user, tempToken, null, authStatus);
    }

    @Mapping(target = "token", source = "token")
    @Mapping(target = "authStatus", source = "authStatus")
    AuthResponse toAuthResponse(User user, String token, List<String> roles, AuthStatus authStatus);

    RegistrationResponse toRegistrationResponse(User user);

    @Mapping(target = "message", source = "message")
    @Mapping(target = "expiresInSeconds", source = "expiresInSeconds")
    OtpResponse toOtpResponse(String message, int expiresInSeconds);

    default List<String> mapRoles(List<AssignedRole> assignedRoles) {
        if (assignedRoles == null) {
            return null;
        }
        return assignedRoles.stream()
                .map(AssignedRole::getRoleName)
                .collect(Collectors.toList());
    }
}
