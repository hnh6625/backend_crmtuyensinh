package com.company.crm_backend.Auth.controller;

import com.company.crm_backend.Auth.application.AuthService;
import com.company.crm_backend.Auth.application.dto.LoginRequest;
import com.company.crm_backend.Auth.application.dto.LoginResponse;
import com.company.crm_backend.Auth.application.dto.RefreshTokenRequest;
import com.company.crm_backend.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request.getRefreshToken()));
    }
}
