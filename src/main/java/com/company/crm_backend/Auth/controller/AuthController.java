package com.company.crm_backend.Auth.controller;

import com.company.crm_backend.Auth.application.AuthService;
import com.company.crm_backend.Auth.application.dto.ChangePasswordRequest;
import com.company.crm_backend.Auth.application.dto.LoginRequest;
import com.company.crm_backend.Auth.application.dto.LoginResponse;
import com.company.crm_backend.Auth.application.dto.RefreshTokenRequest;
import com.company.crm_backend.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest httpReq) {
        return ResponseEntity.ok(
                ApiResponse.success(authService.login(req, httpReq)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success(authService.refresh(req.getRefreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            HttpServletRequest httpReq) {
        Long userId = (Long) httpReq.getAttribute("userId");
        authService.changePassword(userId, req);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse.UserInfo>> getMe(
            HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.success(authService.getMe(userId)));
    }
}