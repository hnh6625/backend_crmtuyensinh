package com.company.crm_backend.importjob.infrastructure.batch;

import com.company.crm_backend.importjob.infrastructure.ImportJobRepository;
import com.company.crm_backend.lead.domain.Lead;
import com.company.crm_backend.lead.infrastructure.LeadRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Bulk INSERT 1000 rows/lần, cập nhật progress và xả RAM an toàn
@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class LeadBatchWriter implements ItemWriter<Lead> {

    private final LeadRepository leadRepository;
    private final ImportJobRepository importJobRepository;

    // Tiêm EntityManager để can thiệp vào bộ nhớ đệm của Hibernate
    private final EntityManager entityManager;

    @Value("#{jobParameters['importId']}")
    private Long importId;

    @Override
    public void write(Chunk<? extends Lead> chunk) {
        // 1. Bulk INSERT — saveAll dùng batch nếu cấu hình hibernate.jdbc.batch_size
        leadRepository.saveAll(chunk.getItems());

        // 2. ÉP HIBERNATE XẢ RAM NGAY LẬP TỨC
        entityManager.flush(); // Bắn ngay các câu lệnh SQL INSERT xuống Database
        entityManager.clear(); // Dọn sạch bộ nhớ đệm L1 Cache, thu hồi RAM cho server

        // 3. Cập nhật progress bằng @Modifying — không load entity về
        importJobRepository.incrementProgress(importId, chunk.size());

        log.info("importId={} — wrote {} leads", importId, chunk.size());
    }
}