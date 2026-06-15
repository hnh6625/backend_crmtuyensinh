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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enrollmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(precision = 15, scale = 2)
    private BigDecimal tuitionFee;

    @Column(precision = 15, scale = 2)
    private BigDecimal scholarshipAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal finalFee;

    private String paymentMethod;
    private String studentCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EnrollmentStatus enrollmentStatus;

    private String conversionSource;
    private LocalDateTime convertedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolled_by")
    private User enrolledBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}