package com.fonepay.devportal.department.service;

import com.fonepay.devportal.department.dto.request.DepartmentRequestDto;
import com.fonepay.devportal.department.dto.response.DepartmentResponseDto;

import java.util.List;

public interface DepartmentService {

    DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto);

    List<DepartmentResponseDto> getAllDepartments(Boolean activeOnly);

    DepartmentResponseDto getDepartmentById(String departmentId);

    DepartmentResponseDto updateDepartment(String departmentId, DepartmentRequestDto requestDto);

    void deleteDepartment(String departmentId);
}