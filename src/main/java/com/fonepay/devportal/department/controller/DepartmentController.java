package com.fonepay.devportal.department.controller;

import com.fonepay.devportal.department.dto.request.DepartmentRequestDto;
import com.fonepay.devportal.department.dto.response.DepartmentResponseDto;
import com.fonepay.devportal.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<DepartmentResponseDto> createDepartment(@Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto created = departmentService.createDepartment(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments(
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        List<DepartmentResponseDto> departments = departmentService.getAllDepartments(activeOnly);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseDto> getDepartmentById(@PathVariable String departmentId) {
        DepartmentResponseDto department = departmentService.getDepartmentById(departmentId);
        return ResponseEntity.ok(department);
    }

    @PutMapping("/{departmentId}")
    public ResponseEntity<DepartmentResponseDto> updateDepartment(
            @PathVariable String departmentId,
            @Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto updated = departmentService.updateDepartment(departmentId, requestDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{departmentId}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String departmentId) {
        departmentService.deleteDepartment(departmentId);
        return ResponseEntity.noContent().build();
    }
}
