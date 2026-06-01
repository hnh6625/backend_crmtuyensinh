package com.company.crm_backend.call.dto;

import com.company.crm_backend.call.domain.FollowUpSchedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FollowUpResponse {
    private Long          scheduleId;
    private Long          leadId;
    private String        leadName;
    private String        leadPhone;
    private Long          consultantId;
    private String        consultantName;
    private LocalDateTime scheduledAt;
    private String        note;
    private Boolean       reminderSent;
    private String        status;
    private LocalDateTime createdAt;

    public static FollowUpResponse from(FollowUpSchedule f) {
        return FollowUpResponse.builder()
                .scheduleId(f.getScheduleId())
                .leadId(f.getLead().getLeadId())
                .leadName(f.getLead().getFullName())
                .leadPhone(f.getLead().getPhone())
                .consultantId(f.getConsultant().getUserId())
                .consultantName(f.getConsultant().getFullName())
                .scheduledAt(f.getScheduledAt())
                .note(f.getNote())
                .reminderSent(f.getReminderSent())
                .status(f.getStatus().name())
                .createdAt(f.getCreatedAt())
                .build();
    }
}