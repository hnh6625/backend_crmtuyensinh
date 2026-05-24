package com.company.crm_backend.User.application.dto;

import com.company.crm_backend.User.domain.UserStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFilterRequest {
    private String     keyword;    // username / fullName / email
    private String     role;       // ADMIN / CONSULTANT / COLLABORATOR
    private UserStatus status;
}