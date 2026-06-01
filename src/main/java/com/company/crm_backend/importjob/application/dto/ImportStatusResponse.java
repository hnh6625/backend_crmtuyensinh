package com.company.crm_backend.importjob.application.dto;

import com.company.crm_backend.importjob.domain.ImportJob;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ImportStatusResponse {

    private Long   importId;
    private String fileName;
    private Long   fileSize;
    private String importStatus;
    private Integer totalRecords;
    private Integer successRecords;
    private Integer failedRecords;
    private Integer processingChunks;
    private Integer progressPercent;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private String importedByName;

    public static ImportStatusResponse from(ImportJob j) {
        int total   = j.getTotalRecords()   != null ? j.getTotalRecords()   : 0;
        int success = j.getSuccessRecords() != null ? j.getSuccessRecords() : 0;
        int failed  = j.getFailedRecords()  != null ? j.getFailedRecords()  : 0;
        int percent = total > 0 ? (int) ((success + failed) * 100.0 / total) : 0;

        return ImportStatusResponse.builder()
                .importId(j.getImportId())
                .fileName(j.getFileName())
                .fileSize(j.getFileSize())
                .importStatus(j.getImportStatus().name())
                .totalRecords(total)
                .successRecords(success)
                .failedRecords(failed)
                .processingChunks(j.getProcessingChunks())
                .progressPercent(Math.min(percent, 100))
                .startedAt(j.getStartedAt())
                .finishedAt(j.getFinishedAt())
                .createdAt(j.getCreatedAt())
                .importedByName(j.getImportedBy() != null
                        ? j.getImportedBy().getFullName() : null)
                .build();
    }
}