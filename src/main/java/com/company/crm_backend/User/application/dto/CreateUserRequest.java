package com.company.crm_backend.User.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;

import java.util.Set;

@Getter
public class CreateUserRequest {

    @NotBlank(message = "Tên đăng nhập không được trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập từ 4-50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Tên đăng nhập chỉ chứa chữ, số và dấu _")
    private String username;

    @NotBlank(message = "Email không được trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Họ tên không được trống")
    @Size(max = 150)
    private String fullName;

    @Pattern(regexp = "^(0|\\+84)[0-9]{8,10}$",
            message = "Số điện thoại không đúng định dạng")
    private String phone;

    @NotNull(message = "Role không được trống")
    private Long roleId;
}