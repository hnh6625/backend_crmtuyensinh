package com.company.crm_backend.Auth.application.dto;

import lombok.Data;
@Data

public class LoginRequest {
    private String username;
    private String password;
}
