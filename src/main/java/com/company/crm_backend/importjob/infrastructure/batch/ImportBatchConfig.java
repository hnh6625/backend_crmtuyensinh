package com.company.crm_backend.importjob.infrastructure.batch;

import com.company.crm_backend.importjob.application.dto.LeadRowDto;
import com.company.crm_backend.lead.domain.Lead;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ImportBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;

    // ── Định nghĩa Job
    @Bean
    public Job importLeadsJob(Step importLeadsStep,
                              ImportJobListener listener) {
        return new JobBuilder("importLeadsJob", jobRepository)
                .listener(listener)
                .start(importLeadsStep)
                .build();
    }

    // ── Định nghĩa Step — chunk 1000 rows/lần
    @Bean
    public Step importLeadsStep(
            FlatFileItemReader<LeadRowDto> reader,
            LeadItemProcessor              processor,
            LeadBatchWriter                writer,
            ImportSkipListener             skipListener) {
        return new StepBuilder("importLeadsStep", jobRepository)
                .<LeadRowDto, Lead>chunk(1000, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(ImportValidationException.class)  // row lỗi → skip, không dừng
                .skipLimit(Integer.MAX_VALUE)
                .listener(skipListener)
                .build();
    }

    // ── Reader — đọc CSV streaming, không load hết file vào RAM
    @Bean
    @StepScope
    public FlatFileItemReader<LeadRowDto> csvReader(
            @Value("#{jobParameters['filePath']}") String filePath) {
        return new FlatFileItemReaderBuilder<LeadRowDto>()
                .name("leadCsvReader")
                .resource(new FileSystemResource(filePath))
                .encoding("UTF-8")
                .linesToSkip(1)       // bỏ dòng header
                .delimited()
                .delimiter(",")
                .names("fullName","phone","email","gender","birthDate",
                        "schoolName","graduationYear","province",
                        "address","note","sourceName")
                .targetType(LeadRowDto.class)
                .build();
    }

    // TaskExecutor — 2 thread song song
    @Bean
    public TaskExecutor importTaskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(2);     // giữ cố định tránh OOM
        exec.setQueueCapacity(10);
        exec.setThreadNamePrefix("import-");
        exec.initialize();
        return exec;
    }
}