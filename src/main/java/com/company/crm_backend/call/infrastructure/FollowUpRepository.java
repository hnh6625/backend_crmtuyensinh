package com.company.crm_backend.call.infrastructure;

import com.company.crm_backend.call.domain.FollowUpSchedule;
import com.company.crm_backend.call.domain.FollowUpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FollowUpRepository extends JpaRepository<FollowUpSchedule, Long> {

    // Lịch hẹn PENDING của consultant — sắp đến trước
    List<FollowUpSchedule> findAllByConsultant_UserIdAndStatusOrderByScheduledAtAsc(
            Long consultantId, FollowUpStatus status);

    // Tất cả lịch hẹn của 1 lead
    List<FollowUpSchedule> findAllByLead_LeadIdOrderByScheduledAtDesc(Long leadId);

    // Lịch hẹn sắp đến trong 30 phút — dùng để gửi reminder
    @Query("""
            SELECT f FROM FollowUpSchedule f
            WHERE f.status = 'PENDING'
              AND f.reminderSent = false
              AND f.scheduledAt BETWEEN :now AND :soon
            """)
    List<FollowUpSchedule> findUpcomingForReminder(
            @Param("now") LocalDateTime now,
            @Param("soon") LocalDateTime soon);

    // Đánh dấu đã gửi reminder
    @Modifying
    @Query("UPDATE FollowUpSchedule f SET f.reminderSent = true WHERE f.scheduleId = :id")
    void markReminderSent(@Param("id") Long id);

    // Huỷ tất cả lịch PENDING khi lead không còn cần follow-up
    @Modifying
    @Query("""
            UPDATE FollowUpSchedule f SET f.status = 'CANCELLED'
            WHERE f.lead.leadId = :leadId AND f.status = 'PENDING'
            """)
    void cancelAllPendingByLeadId(@Param("leadId") Long leadId);

    // Cập nhật cancel những lịch hẹn đang pending mà thời gian nhỏ hơn thời gian hiện tại
    @Modifying
    @Query("UPDATE FollowUpSchedule f SET f.status = 'CANCELLED' WHERE f.status = 'PENDING' AND f.scheduledAt < :now")
    int cancelOverdueFollowUps(@Param("now") LocalDateTime now);
}