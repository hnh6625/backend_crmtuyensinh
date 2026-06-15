package com.company.crm_backend.enrollment.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEnrollmentRequest {

    @NotNull(message = "Lead ID không được trống")
    private Long leadId;

    @NotNull(message = "Khoa/Ngành học không được trống")
    private Long majorId;

    private Double tuitionFee;

    private String paymentMethod;

    private String note;
}