package com.company.crm_backend.enrollment.infrastructure;

import com.company.crm_backend.enrollment.domain.Semester;
import com.company.crm_backend.enrollment.domain.SemesterStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
    // Lấy học kỳ đang mở tuyển sinh cho dropdown
    List<Semester> findAllByStatus(SemesterStatus status);
}