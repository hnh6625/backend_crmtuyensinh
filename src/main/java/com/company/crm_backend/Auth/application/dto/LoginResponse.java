package com.company.crm_backend.Auth.application.dto;

import com.company.crm_backend.User.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class LoginResponse {
    private String    accessToken;
    private String    refreshToken;
    private String    tokenType;
    private long      expiresIn;     // giây
    private UserInfo  user;

    @Getter
    @Builder
    public static class UserInfo {
        private Long   userId;
        private String username;
        private String fullName;
        private String email;
        private String phone;
        private String avatar;
        private String role;
        private Boolean mustChangePassword;

        public static UserInfo from(User u) {
            return UserInfo.builder()
                    .userId(u.getUserId())
                    .username(u.getUsername())
                    .fullName(u.getFullName())
                    .email(u.getEmail())
                    .phone(u.getPhone())
                    .avatar(u.getAvatar())
                    .role(u.getRole().getRoleName())
                    .mustChangePassword(u.getMustChangePassword())
                    .build();
        }
    }
}