package com.company.crm_backend.importjob.domain;


import com.company.crm_backend.User.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name ="imports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "import_id")
    private Long importId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", length = 255)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "total_records")
    @Builder.Default
    private Integer totalRecords = 0;

    @Column(name = "success_records")
    @Builder.Default
    private Integer successRecords = 0;

    @Column(name = "failed_records")
    @Builder.Default
    private Integer failedRecords = 0;

    @Column(name = "batch_size")
    @Builder.Default
    private Integer batchSize = 1000;

    @Column(name = "processing_chunks")
    @Builder.Default
    private Integer processingChunks = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "import_status",
            columnDefinition = "ENUM('PENDING','PROCESSING','COMPLETED','FAILED')")
    @Builder.Default
    private ImportStatus importStatus = ImportStatus.PENDING;

    @Column(name = "started_at")
    private LocalDateTime startedAt;
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imported_by", nullable = false)
    private User importedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
