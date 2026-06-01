package com.company.crm_backend.call.infrastructure;

import com.company.crm_backend.call.domain.CallLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CallLogRepository extends JpaRepository<CallLog, Long> {

    //Đếm số lần đã gọi
    long countByLead_LeadId(Long leadId);

    // Lấy số thứ tự gọi lớn nhất của lead
    @Query("SELECT MAX(c.callAttemptNo) FROM CallLog c WHERE c.lead.leadId = :leadId")
    Optional<Integer> findMaxAttemptByLeadId(@Param("leadId") Long leadId);

    // Lịch sử gọi của 1 lead — mới nhất lên trên
    List<CallLog> findAllByLead_LeadIdOrderByCalledAtDesc(Long leadId);

    // Lịch sử gọi của consultant — có phân trang
    Page<CallLog> findAllByConsultant_UserIdOrderByCalledAtDesc(
            Long consultantId, Pageable pageable);

    // Thống kê theo consultant trong khoảng thời gian
    @Query(value = """
        SELECT
            u.full_name                     AS consultantName,
            COUNT(c.call_id)                AS totalCalls,
            SUM(c.duration_seconds)         AS totalDuration,
            SUM(c.call_attempt_no = 1)      AS firstAttempts,
            SUM(c.call_attempt_no = 2)      AS secondAttempts,
            SUM(c.call_attempt_no = 3)      AS thirdAttempts
        FROM call_logs c
        JOIN users u ON c.consultant_id = u.user_id
        WHERE c.called_at BETWEEN :from AND :to
        GROUP BY c.consultant_id, u.full_name
        ORDER BY totalCalls DESC
        """, nativeQuery = true)
    List<Object[]> statsGroupByConsultant(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);
}