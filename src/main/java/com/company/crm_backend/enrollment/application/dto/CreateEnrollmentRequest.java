package com.company.crm_backend.enrollment.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateEnrollmentRequest {

    @NotNull(message = "Lead không được trống")
    private Long leadId;

    @NotNull(message = "Ngành học không được trống")
    private Long majorId;

    @NotNull(message = "Cơ sở không được trống")
    private Long campusId;

    @NotNull(message = "Học kỳ không được trống")
    private Long semesterId;

    @DecimalMin(value = "0", message = "Học bổng không được âm")
    private BigDecimal scholarshipAmount;

    private String conversionSource;
    private String note;
}