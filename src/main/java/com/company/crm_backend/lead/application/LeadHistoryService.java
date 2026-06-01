package com.company.crm_backend.lead.application;

import com.company.crm_backend.lead.domain.LeadHistory;
import com.company.crm_backend.lead.domain.dto.LeadHistoryResponse;
import com.company.crm_backend.lead.infrastructure.LeadHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LeadHistoryService {

    private final LeadHistoryRepository historyRepository;

    // Ghi 1 dòng lịch sử
    public void record(Long leadId, String actionType,
                       String oldValue, String newValue, Long changedBy) {
        historyRepository.save(LeadHistory.builder()
                .leadId(leadId)
                .actionType(actionType)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(changedBy)
                .build());
    }

    // Lấy toàn bộ lịch sử của lead
    @Transactional(readOnly = true)
    public List<LeadHistoryResponse> getHistory(Long leadId) {
        return historyRepository.findAllByLeadIdOrderByCreatedAtDesc(leadId)
                .stream().map(LeadHistoryResponse::from).toList();
    }
}