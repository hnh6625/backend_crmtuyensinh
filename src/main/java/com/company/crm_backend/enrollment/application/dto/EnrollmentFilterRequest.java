package com.company.crm_backend.enrollment.application.dto;

import com.company.crm_backend.enrollment.domain.EnrollmentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrollmentFilterRequest {
    private String keyword;
    private Long departmentId;
    private Long majorId;
    private EnrollmentStatus status;
}