package com.fonepay.devportal.modules.admin.developer.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fonepay.devportal.modules.admin.developer.dto.response.DeveloperResponseDto;
import com.fonepay.devportal.modules.user.document.AssignedRole;
import com.fonepay.devportal.modules.user.document.User;

@Mapper(componentModel = "spring")
public interface DeveloperMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "companyName", source = "companyName")
    @Mapping(target = "registrationDate", source = "createdAt")
    @Mapping(target = "lastLogin", source = "lastLoginAt")
    @Mapping(target = "accountStatus", source = "status")
    @Mapping(target = "emailVerified", source = "emailVerified")
    @Mapping(target = "roles", source = "roles")
    DeveloperResponseDto toDto(User user);

    List<DeveloperResponseDto> toDtoList(List<User> users);

    default List<String> mapRoles(List<AssignedRole> assignedRoles) {
        if (assignedRoles == null) {
            return Collections.emptyList();
        }
        return assignedRoles.stream()
                .map(AssignedRole::getRoleName)
                .collect(Collectors.toList());
    }
}
