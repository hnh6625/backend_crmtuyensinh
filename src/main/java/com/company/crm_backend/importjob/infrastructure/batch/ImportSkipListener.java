package com.company.crm_backend.importjob.infrastructure.batch;

import com.company.crm_backend.importjob.application.dto.LeadRowDto;
import com.company.crm_backend.importjob.infrastructure.ImportDetailRepository;
import com.company.crm_backend.lead.domain.Lead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

// Ghi từng row bị skip vào import_details
@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class ImportSkipListener implements SkipListener<LeadRowDto, Lead> {

    private final ImportDetailRepository importDetailRepository;

    @Value("#{jobParameters['importId']}")
    private Long importId;

    private final AtomicInteger rowCounter = new AtomicInteger(0);

    @Override
    public void onSkipInProcess(LeadRowDto item, Throwable t) {
        int rowNum = rowCounter.incrementAndGet();
        String status = (t.getMessage() != null
                && t.getMessage().contains("trùng")) ? "SKIPPED" : "FAILED";

        importDetailRepository.insertDetail(
                importId, rowNum, t.getMessage(), status);

        log.warn("importId={} skip row={} reason={}", importId, rowNum, t.getMessage());
    }

    @Override public void onSkipInRead(Throwable t) {
        log.warn("Skip in read: {}", t.getMessage());
    }

    @Override public void onSkipInWrite(Lead item, Throwable t) {
        log.warn("Skip in write phone={} reason={}", item.getPhone(), t.getMessage());
    }
}