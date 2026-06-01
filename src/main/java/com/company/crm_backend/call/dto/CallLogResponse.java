package com.company.crm_backend.call.dto;

import com.company.crm_backend.call.domain.CallLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CallLogResponse {
    private Long          callId;
    private Long          leadId;
    private String        leadName;
    private String        leadPhone;
    private Long          consultantId;
    private String        consultantName;
    private Long          resultId;
    private String        resultName;
    private Integer       callAttemptNo;
    private Integer       durationSeconds;
    private String        note;
    private LocalDateTime calledAt;

    public static CallLogResponse from(CallLog c) {
        return CallLogResponse.builder()
                .callId(c.getCallId())
                .leadId(c.getLead().getLeadId())
                .leadName(c.getLead().getFullName())
                .leadPhone(c.getLead().getPhone())
                .consultantId(c.getConsultant().getUserId())
                .consultantName(c.getConsultant().getFullName())
                .resultId(c.getResult() != null ? c.getResult().getResultId() : null)
                .resultName(c.getResult() != null ? c.getResult().getResultName() : null)
                .callAttemptNo(c.getCallAttemptNo())
                .durationSeconds(c.getDurationSeconds())
                .note(c.getNote())
                .calledAt(c.getCalledAt())
                .build();
    }
}