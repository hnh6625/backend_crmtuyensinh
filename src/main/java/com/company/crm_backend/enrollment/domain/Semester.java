package com.company.crm_backend.enrollment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "semesters")
@Getter
@Setter
@NoArgsConstructor
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "semester_id")
    private Long semesterId;

    @Column(name = "semester_code", nullable = false, unique = true, length = 50)
    private String semesterCode;

    @Column(name = "semester_name", nullable = false, length = 255)
    private String semesterName;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "enrollment_open_date")
    private LocalDate enrollmentOpenDate;

    @Column(name = "enrollment_close_date")
    private LocalDate enrollmentCloseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",
            columnDefinition = "ENUM('UPCOMING','OPEN','CLOSED')")
    private SemesterStatus status = SemesterStatus.UPCOMING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}