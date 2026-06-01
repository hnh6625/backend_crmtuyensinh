package com.company.crm_backend.importjob.infrastructure;

import com.company.crm_backend.importjob.domain.ImportJob;
import com.company.crm_backend.importjob.domain.ImportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    // Kiểm tra có job đang chạy không
    boolean existsByImportStatus(ImportStatus status);

    // Lịch sử import — mới nhất lên trên
    Page<ImportJob> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Cập nhật sang PROCESSING + ghi total
    @Modifying
    @Query("""
        UPDATE ImportJob j SET
            j.importStatus = :status,
            j.startedAt    = :startedAt,
            j.totalRecords = :total
        WHERE j.importId = :importId
        """)
    void markProcessing(@Param("importId")  Long importId,
                        @Param("status")    ImportStatus status,
                        @Param("startedAt") LocalDateTime startedAt,
                        @Param("total")     int total);

    // Cập nhật progress sau mỗi chunk
    @Modifying
    @Query("""
        UPDATE ImportJob j SET
            j.successRecords   = j.successRecords + :count,
            j.processingChunks = j.processingChunks + 1
        WHERE j.importId = :importId
        """)
    void incrementProgress(@Param("importId") Long importId,
                           @Param("count")    int count);

    // Cập nhật khi job xong
    @Modifying
    @Query("""
        UPDATE ImportJob j SET
            j.importStatus  = :status,
            j.finishedAt    = :finishedAt,
            j.failedRecords = :failed
        WHERE j.importId = :importId
        """)
    void markFinished(@Param("importId")   Long importId,
                      @Param("status")     ImportStatus status,
                      @Param("finishedAt") LocalDateTime finishedAt,
                      @Param("failed")     int failed);
}