package com.fonepay.devportal.modules.department.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fonepay.devportal.modules.department.dto.response.DepartmentResponseDto;
import com.fonepay.devportal.modules.department.entity.Department;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(source = "active", target = "isActive")
    DepartmentResponseDto toResponseDto(Department department);

    List<DepartmentResponseDto> toResponseDtoList(List<Department> departments);
}
