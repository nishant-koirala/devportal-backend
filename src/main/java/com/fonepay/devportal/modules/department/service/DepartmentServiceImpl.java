package com.fonepay.devportal.modules.department.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.DuplicateResourceException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.department.dto.request.DepartmentRequestDto;
import com.fonepay.devportal.modules.department.dto.response.DepartmentResponseDto;
import com.fonepay.devportal.modules.department.entity.Department;
import com.fonepay.devportal.modules.department.mapper.DepartmentMapper;
import com.fonepay.devportal.modules.department.repository.DepartmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final Clock clock;

    @Override
    public List<DepartmentResponseDto> listActiveDepartments() {
        return departmentRepository.findByIsActiveTrue().stream()
                .map(departmentMapper::toDto)
                .toList();
    }

    @Override
    public DepartmentResponseDto createDepartment(DepartmentRequestDto request) {
        String name = request.getDepartmentName().trim();
        if (departmentRepository.existsByDepartmentNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Department already exists: " + name);
        }

        Department department = Department.builder()
                .departmentId(IdGenerator.nextUlid())
                .departmentName(name)
                .departmentDescription(request.getDepartmentDescription())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .createdAt(Instant.now(clock))
                .build();

        department = departmentRepository.save(department);
        log.info("Created department '{}' ({})", department.getDepartmentName(), department.getDepartmentId());
        return departmentMapper.toDto(department);
    }

    @Override
    public Department requireActiveDepartment(String departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (!department.isActive()) {
            throw new BadRequestException("Department is not active");
        }
        return department;
    }
}
