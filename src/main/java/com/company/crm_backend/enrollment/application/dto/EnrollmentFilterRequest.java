package com.company.crm_backend.enrollment.application.dto;

import com.company.crm_backend.enrollment.domain.EnrollmentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrollmentFilterRequest {
    private Long semesterId;
    private Long majorId;
    private Long campusId;
    private EnrollmentStatus enrollmentStatus;
    private String keyword;
}