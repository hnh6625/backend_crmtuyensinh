package com.company.crm_backend.Auth.application.dto;

import lombok.Data;

@Data
@lombok.AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}
