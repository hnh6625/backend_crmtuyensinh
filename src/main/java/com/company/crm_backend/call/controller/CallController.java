package com.company.crm_backend.call.controller;

import com.company.crm_backend.call.application.CallService;
import com.company.crm_backend.call.domain.CallResult;
import com.company.crm_backend.call.dto.CallLogResponse;
import com.company.crm_backend.call.dto.CreateCallLogRequest;
import com.company.crm_backend.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final CallService callService;

    // Dropdown kết quả cuộc gọi
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<CallResult>>> getResults() {
        return ResponseEntity.ok(ApiResponse.success(callService.getCallResults()));
    }

    // Ghi nhận cuộc gọi
    @PostMapping
    public ResponseEntity<ApiResponse<CallLogResponse>> createCallLog(
            @Valid @RequestBody CreateCallLogRequest req,
            HttpServletRequest httpReq) {
        Long userId = (Long) httpReq.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(callService.createCallLog(req, userId)));
    }

    // Lịch sử gọi của lead
    @GetMapping("/lead/{leadId}")
    public ResponseEntity<ApiResponse<List<CallLogResponse>>> getByLead(
            @PathVariable Long leadId) {
        return ResponseEntity.ok(ApiResponse.success(
                callService.getCallsByLead(leadId)));
    }

    // Lịch sử gọi của tôi
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<CallLogResponse>>> getMyCalls(
            @PageableDefault(size = 20, sort = "calledAt",
                    direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest httpReq) {
        Long userId = (Long) httpReq.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.success(
                callService.getCallsByConsultant(userId, pageable)));
    }
}