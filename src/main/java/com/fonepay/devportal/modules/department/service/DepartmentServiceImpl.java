package com.fonepay.devportal.modules.department.service;

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
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto) {
        log.info("Creating department with name: {}", requestDto.getDepartmentName());

        if (departmentRepository.existsByDepartmentNameIgnoreCase(requestDto.getDepartmentName().trim())) {
            throw new DuplicateResourceException("Department with name '" + requestDto.getDepartmentName() + "' already exists");
        }

        Department department = Department.builder()
                .departmentId(IdGenerator.nextUlid())
                .departmentName(requestDto.getDepartmentName().trim())
                .departmentDescription(requestDto.getDepartmentDescription())
                .isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true)
                .createdAt(Instant.now())
                .build();

        Department savedDepartment = departmentRepository.save(department);
        log.info("Department created successfully with ID: {}", savedDepartment.getDepartmentId());
        return departmentMapper.toResponseDto(savedDepartment);
    }

    @Override
    public List<DepartmentResponseDto> getAllDepartments(Boolean activeOnly) {
        List<Department> departments;
        if (Boolean.TRUE.equals(activeOnly)) {
            departments = departmentRepository.findByIsActiveTrue();
        } else {
            departments = departmentRepository.findAll();
        }

        return departmentMapper.toResponseDtoList(departments);
    }

    @Override
    public DepartmentResponseDto getDepartmentById(String departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));
        return departmentMapper.toResponseDto(department);
    }

    @Override
    public DepartmentResponseDto updateDepartment(String departmentId, DepartmentRequestDto requestDto) {
        log.info("Updating department with ID: {}", departmentId);

        Department existingDepartment = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));

        String newName = requestDto.getDepartmentName().trim();
        if (!existingDepartment.getDepartmentName().equalsIgnoreCase(newName)
                && departmentRepository.existsByDepartmentNameIgnoreCase(newName)) {
            throw new DuplicateResourceException("Department with name '" + newName + "' already exists");
        }

        existingDepartment.setDepartmentName(newName);
        existingDepartment.setDepartmentDescription(requestDto.getDepartmentDescription());
        if (requestDto.getIsActive() != null) {
            existingDepartment.setActive(requestDto.getIsActive());
        }

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        log.info("Department updated successfully with ID: {}", updatedDepartment.getDepartmentId());
        return departmentMapper.toResponseDto(updatedDepartment);
    }

    @Override
    public void deleteDepartment(String departmentId) {
        log.info("Deleting department with ID: {}", departmentId);
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with ID: " + departmentId);
        }
        departmentRepository.deleteById(departmentId);
        log.info("Department deleted with ID: {}", departmentId);
    }
}
