package com.company.crm_backend.enrollment.infrastructure;

import com.company.crm_backend.enrollment.domain.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MajorRepository extends JpaRepository<Major, Long> {
    // Lấy ngành đang hoạt động cho dropdown
    List<Major> findAllByIsActiveTrue();
    Optional<Major> findByMajorCode(String majorCode);
}