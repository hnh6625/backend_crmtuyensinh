package com.company.crm_backend.importjob.application;

import com.company.crm_backend.User.domain.User;
import com.company.crm_backend.User.infrastructure.UserRepository;
import com.company.crm_backend.importjob.application.dto.ImportStatusResponse;
import com.company.crm_backend.importjob.domain.ImportDetail;
import com.company.crm_backend.importjob.domain.ImportJob;
import com.company.crm_backend.importjob.domain.ImportStatus;
import com.company.crm_backend.importjob.infrastructure.ImportDetailRepository;
import com.company.crm_backend.importjob.infrastructure.ImportJobRepository;
import com.company.crm_backend.shared.exception.AppException;
import com.company.crm_backend.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final ImportJobRepository importJobRepository;
    private final ImportDetailRepository importDetailRepository;
    private final UserRepository userRepository;
    private final JobLauncher jobLauncher;
    private final Job importLeadsJob;

    @Value("${app.import.upload-dir:uploads/imports}")
    private String uploadDir;

    // 1. Upload file CSV → lưu disk → trigger batch job
    @Transactional
    public ImportStatusResponse upload(MultipartFile file, Long userId) {
        validateFile(file);

        String filePath = saveFileToDisk(file);

        User uploader = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ImportJob job = ImportJob.builder()
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileSize(file.getSize())
                .importedBy(uploader)
                .build();

        importJobRepository.save(job);

        // Trigger async — trả về ngay, batch chạy ngầm
        triggerBatch(job.getImportId(), filePath);

        log.info("Import triggered: id={}, file={}", job.getImportId(),
                file.getOriginalFilename());
        return ImportStatusResponse.from(job);
    }

    // 2. Polling trạng thái — Vue gọi mỗi 2 giây
    @Transactional(readOnly = true)
    public ImportStatusResponse getStatus(Long importId) {
        return importJobRepository.findById(importId)
                .map(ImportStatusResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.IMPORT_NOT_FOUND));
    }

    // 3. Lịch sử import
    @Transactional(readOnly = true)
    public Page<ImportStatusResponse> getHistory(Pageable pageable) {
        return importJobRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(ImportStatusResponse::from);
    }

    // 4. Danh sách row lỗi
    @Transactional(readOnly = true)
    public List<ImportDetail> getErrors(Long importId) {
        return importDetailRepository
                .findByImportJob_ImportIdOrderByRowNum(importId);
    }

    // Helpers
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT);

        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".csv"))
            throw new AppException(ErrorCode.INVALID_FILE_FORMAT);

        // Giới hạn 50MB
        if (file.getSize() > 50L * 1024 * 1024)
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
    }

    private String saveFileToDisk(MultipartFile file) {
        try {
            Path dir = Path.of(uploadDir);
            Files.createDirectories(dir);
            String name = System.currentTimeMillis() + "_"
                    + file.getOriginalFilename();
            Path dest = dir.resolve(name);
            file.transferTo(dest.toFile());
            return dest.toString();
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Async
    public void triggerBatch(Long importId, String filePath) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("importId",   importId)
                    .addString("filePath", filePath)
                    .addLong("time",       System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(importLeadsJob, params);
        } catch (Exception e) {
            log.error("Batch trigger failed: importId={}", importId, e);
            importJobRepository.findById(importId).ifPresent(j -> {
                j.setImportStatus(ImportStatus.FAILED);
                importJobRepository.save(j);
            });
        }
    }
}