package com.company.crm_backend.importjob.infrastructure;

import com.company.crm_backend.importjob.domain.ImportDetail;
import com.company.crm_backend.importjob.domain.ProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImportDetailRepository extends JpaRepository<ImportDetail, Long> {

    // Lấy danh sách row lỗi theo import — sắp xếp theo row_num
    List<ImportDetail> findByImportJob_ImportIdOrderByRowNum(Long importId);

    // Đếm theo loại lỗi
    long countByImportJob_ImportIdAndProcessStatus(Long importId,
                                                   ProcessStatus status);

    // Insert nhanh từng row lỗi — không cần load entity
    @Modifying
    @Query(value = """
            INSERT INTO import_details
                (import_id, row_num, validation_errors, process_status, created_at)
            VALUES
                (:importId, :rowNum, :errors, :status, NOW())
            """, nativeQuery = true)
    void insertDetail(@Param("importId") Long importId,
                      @Param("rowNum") int rowNum,
                      @Param("errors") String errors,
                      @Param("status") String status);
}