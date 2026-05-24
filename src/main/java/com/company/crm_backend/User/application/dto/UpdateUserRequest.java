package com.company.crm_backend.User.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateUserRequest {

    @Size(max = 150)
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    private String email;

    @Pattern(regexp = "^(0|\\+84)[0-9]{8,10}$",
            message = "Số điện thoại không đúng định dạng")
    private String phone;

    private String avatar;
    private Long   roleId;
}