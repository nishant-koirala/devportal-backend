package com.fonepay.devportal.modules.department.service;

import java.util.List;

import com.fonepay.devportal.modules.department.dto.request.DepartmentRequestDto;
import com.fonepay.devportal.modules.department.dto.response.DepartmentResponseDto;
import com.fonepay.devportal.modules.department.entity.Department;

public interface DepartmentService {

    List<DepartmentResponseDto> listActiveDepartments();

    DepartmentResponseDto createDepartment(DepartmentRequestDto request);

    Department requireActiveDepartment(String departmentId);
}
