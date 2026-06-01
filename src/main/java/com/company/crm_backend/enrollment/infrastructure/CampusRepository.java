package com.company.crm_backend.enrollment.infrastructure;

import com.company.crm_backend.enrollment.domain.Campus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampusRepository extends JpaRepository<Campus, Long> {
    // Chỉ lấy cơ sở đang hoạt động cho dropdown
    List<Campus> findAllByIsActiveTrue();
}