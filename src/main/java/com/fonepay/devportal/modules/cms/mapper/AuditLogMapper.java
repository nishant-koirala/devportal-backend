package com.fonepay.devportal.modules.cms.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.fonepay.devportal.modules.cms.document.AuditLog;
import com.fonepay.devportal.modules.cms.dto.response.AuditLogResponseDto;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogResponseDto toResponseDto(AuditLog auditLog);

    List<AuditLogResponseDto> toResponseDtoList(List<AuditLog> auditLogs);
}
