package com.fonepay.devportal.modules.department.controller;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fonepay.devportal.common.constant.apis.ApiRoutes;
import com.fonepay.devportal.common.dto.ApiResponse;
import com.fonepay.devportal.modules.department.dto.request.DepartmentRequestDto;
import com.fonepay.devportal.modules.department.dto.response.DepartmentResponseDto;
import com.fonepay.devportal.modules.department.service.DepartmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import com.fonepay.devportal.security.Permissions;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping(ApiRoutes.Admin.DEPARTMENTS)
@PreAuthorize("hasAuthority('" + Permissions.SYSTEM_MANAGE + "')")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final Clock clock;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponseDto>>> listActiveDepartments() {
        List<DepartmentResponseDto> departments = departmentService.listActiveDepartments();
        return ResponseEntity.ok(
                ApiResponse.<List<DepartmentResponseDto>>builder()
                        .status(HttpStatus.OK.value())
                        .success(true)
                        .message("Departments retrieved successfully")
                        .data(departments)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponseDto>> createDepartment(
            @Valid @RequestBody DepartmentRequestDto request) {
        DepartmentResponseDto created = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<DepartmentResponseDto>builder()
                        .status(HttpStatus.CREATED.value())
                        .success(true)
                        .message("Department created successfully")
                        .data(created)
                        .timestamp(LocalDateTime.now(clock))
                        .build());
    }
}
