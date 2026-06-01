package com.company.crm_backend.call.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateCallLogRequest {

    @NotNull(message = "Lead không được trống")
    private Long leadId;

    @NotNull(message = "Kết quả cuộc gọi không được trống")
    private Long resultId;

    @Min(value = 0, message = "Thời lượng không được âm")
    private Integer durationSeconds;

    private String note;
}