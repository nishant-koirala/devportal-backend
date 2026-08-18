package com.fonepay.devportal.modules.department.service;

import java.util.List;

import com.fonepay.devportal.modules.department.dto.request.DepartmentRequestDto;
import com.fonepay.devportal.modules.department.dto.response.DepartmentResponseDto;

public interface DepartmentService {

    DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto);

    List<DepartmentResponseDto> getAllDepartments(Boolean activeOnly);

    DepartmentResponseDto getDepartmentById(String departmentId);

    DepartmentResponseDto updateDepartment(String departmentId, DepartmentRequestDto requestDto);

    void deleteDepartment(String departmentId);
}