package com.company.crm_backend.lead.infrastructure;

import com.company.crm_backend.lead.domain.Lead;
import com.company.crm_backend.lead.domain.dto.LeadStatusStatDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LeadRepository
        extends JpaRepository<Lead, Long>,
        JpaSpecificationExecutor<Lead> {

    // Tìm lead chưa xóa
    Optional<Lead> findByLeadIdAndDeletedAtIsNull(Long leadId);

    // Kiểm tra trùng SĐT
    boolean existsByPhoneNormalizedAndDeletedAtIsNull(String phoneNormalized);

    // Dùng cho ImportJob
    @Query("SELECT l.phoneNormalized FROM Lead l " +
            "WHERE l.deletedAt IS NULL AND l.phoneNormalized IS NOT NULL")
    List<String> findAllPhoneNormalized();

    // Cập nhật sau mỗi cuộc gọi
    @Modifying
    @Query("UPDATE Lead l SET l.lastCalledAt = :calledAt WHERE l.leadId = :leadId")
    void updateLastCalledAt(@Param("leadId")   Long leadId,
                            @Param("calledAt") LocalDateTime calledAt);

    // ── Cập nhật lịch follow-up
    @Modifying
    @Query("UPDATE Lead l SET l.nextFollowUpAt = :followUpAt WHERE l.leadId = :leadId")
    void updateNextFollowUpAt(@Param("leadId")     Long leadId,
                              @Param("followUpAt") LocalDateTime followUpAt);

    // Soft delete
    @Modifying
    @Query("UPDATE Lead l SET l.deletedAt = :now WHERE l.leadId = :leadId")
    void softDelete(@Param("leadId") Long leadId,
                    @Param("now")    LocalDateTime now);

    @Query("SELECT new com.company.crm_backend.lead.domain.dto.LeadStatusStatDto(ls.statusName, COUNT(l)) " +
            "FROM Lead l JOIN l.status ls GROUP BY ls.statusName")
    List<LeadStatusStatDto> countLeadsByStatus();
}