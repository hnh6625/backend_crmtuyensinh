package com.company.crm_backend.lead.infrastructure;

import com.company.crm_backend.lead.domain.LeadHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadHistoryRepository extends JpaRepository<LeadHistory, Long> {
    List<LeadHistory> findAllByLeadIdOrderByCreatedAtDesc(Long leadId);
}