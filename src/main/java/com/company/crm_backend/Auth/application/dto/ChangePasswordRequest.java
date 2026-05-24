package com.company.crm_backend.Auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChangePasswordRequest {

    @NotBlank(message = "Mật khẩu cũ không được trống")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được trống")
    @Size(min = 6, message = "Mật khẩu mới tối thiểu 6 ký tự")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu không được trống")
    private String confirmPassword;
}