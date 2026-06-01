package com.company.crm_backend.importjob.infrastructure.batch;

import com.company.crm_backend.importjob.domain.ImportStatus;
import com.company.crm_backend.importjob.infrastructure.ImportJobRepository;
import com.company.crm_backend.lead.infrastructure.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// Quản lý trạng thái ImportJob trước và sau khi Batch chạy
@Component
@RequiredArgsConstructor
@Slf4j
public class ImportJobListener implements JobExecutionListener {

    private final ImportJobRepository importJobRepository;
    private final LeadRepository leadRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        Long   importId = jobExecution.getJobParameters().getLong("importId");
        String filePath = jobExecution.getJobParameters().getString("filePath");

        // Đếm tổng số row (trừ header)
        int total = countRows(filePath);

        // Load toàn bộ phone_normalized vào Set — 100k × 12 char ≈ 1.2MB RAM
        Set<String> phones = new HashSet<>(leadRepository.findAllPhoneNormalized());

        // Đưa vào context để Processor dùng
        jobExecution.getExecutionContext().put("existingPhones", phones);

        // Cập nhật trạng thái PROCESSING
        importJobRepository.markProcessing(
                importId, ImportStatus.PROCESSING, LocalDateTime.now(), total);

        log.info("Import started: id={}, total={}, existingPhones={}",
                importId, total, phones.size());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Long importId = jobExecution.getJobParameters().getLong("importId");

        ImportStatus finalStatus = jobExecution.getStatus() == BatchStatus.COMPLETED
                ? ImportStatus.COMPLETED : ImportStatus.FAILED;

        // Tính failed = total - success
        importJobRepository.findById(importId).ifPresent(job -> {
            int failed = job.getTotalRecords() - job.getSuccessRecords();
            importJobRepository.markFinished(
                    importId, finalStatus,
                    LocalDateTime.now(), Math.max(0, failed));
        });

        log.info("Import finished: id={}, status={}", importId, finalStatus);
    }

    private int countRows(String path) {
        try (var lines = Files.lines(Path.of(path))) {
            return (int) lines.count() - 1;
        } catch (Exception e) {
            log.warn("Cannot count rows: {}", e.getMessage());
            return 0;
        }
    }
}