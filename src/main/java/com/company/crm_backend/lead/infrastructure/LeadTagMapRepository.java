package com.company.crm_backend.lead.infrastructure;

import com.company.crm_backend.lead.domain.LeadTagMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeadTagMapRepository extends JpaRepository<LeadTagMap, Long> {
    List<LeadTagMap> findAllByLead_LeadId(Long leadId);

    @Modifying
    @Query("DELETE FROM LeadTagMap m WHERE m.lead.leadId = :leadId")
    void deleteAllByLeadId(@Param("leadId") Long leadId);
}