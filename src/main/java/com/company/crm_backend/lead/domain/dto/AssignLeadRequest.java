package com.company.crm_backend.lead.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AssignLeadRequest {

    @NotNull(message = "User không được trống")
    private Long assignToUserId;

    private String note;
}