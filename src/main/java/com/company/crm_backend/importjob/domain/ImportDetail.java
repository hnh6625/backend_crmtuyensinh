package com.company.crm_backend.importjob.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id", nullable = false)
    private ImportJob importJob;

    @Column(name = "row_num", nullable = false)
    private Integer rowNum;

    @Column(name = "validation_errors", columnDefinition = "TEXT")
    private String validationErrors;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_status",
            columnDefinition = "ENUM('FAILED','SKIPPED')")
    @Builder.Default
    private ProcessStatus processStatus = ProcessStatus.FAILED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}