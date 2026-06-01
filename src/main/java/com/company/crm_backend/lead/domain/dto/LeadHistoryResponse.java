package com.company.crm_backend.lead.domain.dto;

import com.company.crm_backend.lead.domain.LeadHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LeadHistoryResponse {
    private Long historyId;
    private String actionType;
    private String oldValue;
    private String newValue;
    private Long changedBy;
    private LocalDateTime createdAt;

    public static LeadHistoryResponse from(LeadHistory h) {
        return LeadHistoryResponse.builder()
                .historyId(h.getHistoryId())
                .actionType(h.getActionType())
                .oldValue(h.getOldValue())
                .newValue(h.getNewValue())
                .changedBy(h.getChangedBy())
                .createdAt(h.getCreatedAt())
                .build();
    }
}