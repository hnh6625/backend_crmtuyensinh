package com.company.crm_backend.User.controller;

import com.company.crm_backend.shared.response.ApiResponse;
import com.company.crm_backend.User.application.UserService;
import com.company.crm_backend.User.application.dto.CreateUserRequest;
import com.company.crm_backend.User.application.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(userService.getUserProfile(userDetails.getUsername()));
    }
}
