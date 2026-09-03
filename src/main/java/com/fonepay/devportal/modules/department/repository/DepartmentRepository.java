package com.fonepay.devportal.modules.department.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.department.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    Optional<Department> findByDepartmentNameIgnoreCase(String departmentName);

    boolean existsByDepartmentNameIgnoreCase(String departmentName);

    @Query("SELECT d FROM Department d WHERE d.isActive = true")
    List<Department> findByIsActiveTrue();
}
