package com.company.crm_backend.enrollment.application.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UpdateEnrollmentRequest {
    private Long majorId;
    private BigDecimal scholarshipAmount;
    private String studentCode;
    private String note;
}