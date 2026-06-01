package com.company.crm_backend.lead.infrastructure;

import com.company.crm_backend.lead.domain.LeadSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeadSourceRepository extends JpaRepository<LeadSource, Long> {
    Optional<LeadSource> findBySourceName(String sourceName);
}
