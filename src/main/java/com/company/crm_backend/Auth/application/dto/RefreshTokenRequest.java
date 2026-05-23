package com.company.crm_backend.Auth.application.dto;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
