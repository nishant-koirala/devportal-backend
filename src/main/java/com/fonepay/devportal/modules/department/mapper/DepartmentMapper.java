package com.fonepay.devportal.modules.department.mapper;

import org.springframework.stereotype.Component;

import com.fonepay.devportal.modules.department.dto.response.DepartmentResponseDto;
import com.fonepay.devportal.modules.department.entity.Department;

@Component
public class DepartmentMapper {

    public DepartmentResponseDto toDto(Department department) {
        if (department == null) {
            return null;
        }
        return DepartmentResponseDto.builder()
                .departmentId(department.getDepartmentId())
                .departmentName(department.getDepartmentName())
                .departmentDescription(department.getDepartmentDescription())
                .isActive(department.isActive())
                .createdAt(department.getCreatedAt())
                .build();
    }
}
