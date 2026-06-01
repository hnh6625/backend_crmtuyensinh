package com.company.crm_backend.enrollment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "majors")
@Getter
@Setter
@NoArgsConstructor
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "major_id")
    private Long majorId;

    @Column(name = "major_code", nullable = false, unique = true, length = 50)
    private String majorCode;

    @Column(name = "major_name", nullable = false, length = 255)
    private String majorName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "tuition_fee", precision = 15, scale = 2)
    private BigDecimal tuitionFee;

    @Column(name = "duration_years")
    private Integer durationYears;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}