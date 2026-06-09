package com.company.crm_backend.lead.infrastructure;

import com.company.crm_backend.lead.domain.LeadTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeadTagRepository extends JpaRepository<LeadTag, Long> {
    List<LeadTag> findAllByTagIdIn(List<Long> tagIds);
    Optional<LeadTag> findByTagName(String tagName);
}