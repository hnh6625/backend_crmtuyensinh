package com.company.crm_backend.User.application.dto;

import com.company.crm_backend.User.domain.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String avatar;
    private String role;
    private Long roleId;
    private String status;
    private Boolean mustChangePassword;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User u) {
        return UserResponse.builder()
                .userId(u.getUserId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .phone(u.getPhone())
                .avatar(u.getAvatar())
                .role(u.getRole().getRoleName())
                .roleId(u.getRole().getRoleId())
                .status(u.getStatus().name())
                .mustChangePassword(u.getMustChangePassword())
                .lastLoginAt(u.getLastLoginAt())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}