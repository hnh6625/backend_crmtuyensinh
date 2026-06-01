package com.company.crm_backend.enrollment.domain;

import com.company.crm_backend.User.domain.User;
import com.company.crm_backend.lead.domain.Lead;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    // 1 lead chỉ nhập học 1 lần — UNIQUE
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false, unique = true)
    private Lead lead;

    @Column(name = "student_code", unique = true, length = 50)
    private String studentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrollment_status",
            columnDefinition = "ENUM('PENDING','CONFIRMED','CANCELLED','COMPLETED')")
    @Builder.Default
    private EnrollmentStatus enrollmentStatus = EnrollmentStatus.PENDING;

    @Column(name = "tuition_fee", precision = 15, scale = 2)
    private BigDecimal tuitionFee;

    @Column(name = "scholarship_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal scholarshipAmount = BigDecimal.ZERO;

    @Column(name = "final_fee", precision = 15, scale = 2)
    private BigDecimal finalFee;

    @Column(name = "conversion_source", length = 100)
    private String conversionSource;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolled_by")
    private User enrolledBy;

    @CreationTimestamp
    @Column(name = "enrollment_date", updatable = false)
    private LocalDateTime enrollmentDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}