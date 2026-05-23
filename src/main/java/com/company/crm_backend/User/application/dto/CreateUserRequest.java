package com.company.crm_backend.User.application.dto;

import lombok.Data;
import java.util.Set;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private Set<String> roles;
}

