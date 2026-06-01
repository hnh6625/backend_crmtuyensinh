package com.company.crm_backend.call.controller;

import com.company.crm_backend.call.application.CallService;
import com.company.crm_backend.call.domain.FollowUpStatus;
import com.company.crm_backend.call.dto.CreateFollowUpRequest;
import com.company.crm_backend.call.dto.FollowUpResponse;
import com.company.crm_backend.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final CallService callService;

    // Tạo lịch hẹn
    @PostMapping
    public ResponseEntity<ApiResponse<FollowUpResponse>> create(
            @Valid @RequestBody CreateFollowUpRequest req,
            HttpServletRequest httpReq) {
        Long userId = (Long) httpReq.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(callService.createFollowUp(req, userId)));
    }

    // Lịch hẹn PENDING của tôi
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getMyPending(
            HttpServletRequest httpReq) {
        Long userId = (Long) httpReq.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.success(
                callService.getMyPendingFollowUps(userId)));
    }

    // Lịch hẹn của 1 lead
    @GetMapping("/lead/{leadId}")
    public ResponseEntity<ApiResponse<List<FollowUpResponse>>> getByLead(
            @PathVariable Long leadId) {
        return ResponseEntity.ok(ApiResponse.success(
                callService.getFollowUpsByLead(leadId)));
    }

    // Cập nhật trạng thái
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<FollowUpResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam FollowUpStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                callService.updateFollowUpStatus(id, status)));
    }
}