package com.company.crm_backend.call.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateFollowUpRequest {

    @NotNull(message = "Lead không được trống")
    private Long leadId;

    @NotNull(message = "Thời gian hẹn không được trống")
    @Future(message = "Thời gian hẹn phải trong tương lai")
    private LocalDateTime scheduledAt;

    private String note;
}