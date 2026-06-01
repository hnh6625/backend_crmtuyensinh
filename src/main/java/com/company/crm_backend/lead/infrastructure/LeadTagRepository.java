package com.company.crm_backend.lead.infrastructure;

import com.company.crm_backend.lead.domain.LeadTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadTagRepository extends JpaRepository<LeadTag, Long> {
    List<LeadTag> findAllByTagIdIn(List<Long> tagIds);
}