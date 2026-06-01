package com.company.crm_backend.lead.infrastructure;

import com.company.crm_backend.lead.domain.LeadAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadAssignmentRepository extends JpaRepository<LeadAssignment, Long> {
    List<LeadAssignment> findAllByLead_LeadIdOrderByAssignedAtDesc(Long leadId);
}