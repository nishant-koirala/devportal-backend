package com.fonepay.devportal.modules.department.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.department.entity.Department;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {

    Optional<Department> findByDepartmentNameIgnoreCase(String departmentName);

    boolean existsByDepartmentNameIgnoreCase(String departmentName);

    List<Department> findByIsActiveTrue();
}
