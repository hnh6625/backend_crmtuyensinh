package com.company.crm_backend.lead.infrastructure;

import com.company.crm_backend.lead.domain.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeadStatusRepository extends JpaRepository<LeadStatus, Long> {
    Optional<LeadStatus> findByStatusName(String statusName);
}