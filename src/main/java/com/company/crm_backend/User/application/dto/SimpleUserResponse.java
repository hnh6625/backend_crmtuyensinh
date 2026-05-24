package com.company.crm_backend.User.application.dto;

import com.company.crm_backend.User.domain.User;
import lombok.Builder;
import lombok.Getter;

// Dùng cho dropdown assign lead
@Getter
@Builder
public class SimpleUserResponse {
    private Long   userId;
    private String fullName;
    private String username;
    private String role;

    public static SimpleUserResponse from(User u) {
        return SimpleUserResponse.builder()
                .userId(u.getUserId())
                .fullName(u.getFullName())
                .username(u.getUsername())
                .role(u.getRole().getRoleName())
                .build();
    }
}