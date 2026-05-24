package com.company.crm_backend.User.application.dto;

import com.company.crm_backend.User.domain.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateStatusRequest {

    @NotNull(message = "Trạng thái không được trống")
    private UserStatus status;
}