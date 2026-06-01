package com.company.crm_backend.importjob.infrastructure.batch;

import com.company.crm_backend.importjob.infrastructure.ImportJobRepository;
import com.company.crm_backend.lead.domain.Lead;
import com.company.crm_backend.lead.infrastructure.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Bulk INSERT 1000 rows/lần, cập nhật progress
@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class LeadBatchWriter implements ItemWriter<Lead> {

    private final LeadRepository leadRepository;
    private final ImportJobRepository importJobRepository;

    @Value("#{jobParameters['importId']}")
    private Long importId;

    @Override
    public void write(Chunk<? extends Lead> chunk) {
        // Bulk INSERT — saveAll dùng batch nếu cấu hình hibernate.jdbc.batch_size
        leadRepository.saveAll(chunk.getItems());

        // Cập nhật progress bằng @Modifying — không load entity về
        importJobRepository.incrementProgress(importId, chunk.size());

        log.info("importId={} — wrote {} leads", importId, chunk.size());
    }
}