package com.company.crm_backend.User.application.dto;

import lombok.*;
import java.util.Set;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private boolean active;
    private Set<String> roles;
}
