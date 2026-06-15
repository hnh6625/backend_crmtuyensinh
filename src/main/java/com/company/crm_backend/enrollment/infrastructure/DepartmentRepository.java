package com.company.crm_backend.enrollment.infrastructure;

import com.company.crm_backend.enrollment.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findAllByIsActiveTrue();
}