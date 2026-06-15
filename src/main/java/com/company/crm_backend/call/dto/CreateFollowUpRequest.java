package com.company.crm_backend.call.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateFollowUpRequest {

    @NotNull(message = "Lead không được trống")
    private Long leadId;

    @NotNull(message = "Thời gian hẹn không được trống")
    @Future(message = "Thời gian hẹn phải trong tương lai")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledAt;

    private String note;
}