package com.company.crm_backend.lead.controller;

import com.company.crm_backend.lead.application.LeadService;
import com.company.crm_backend.lead.domain.LeadSource;
import com.company.crm_backend.lead.domain.LeadStatus;
import com.company.crm_backend.lead.domain.LeadTag;
import com.company.crm_backend.lead.domain.dto.*;
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
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    // Dropdown data
    @GetMapping("/sources")
    public ResponseEntity<ApiResponse<List<LeadSource>>> getSources() {
        return ResponseEntity.ok(ApiResponse.success(leadService.getSources()));
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<LeadStatus>>> getStatuses() {
        return ResponseEntity.ok(ApiResponse.success(leadService.getStatuses()));
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<LeadTag>>> getTags() {
        return ResponseEntity.ok(ApiResponse.success(leadService.getTags()));
    }

    // Danh sách lead
    @GetMapping
    public ResponseEntity<ApiResponse<Page<LeadResponse>>> getList(
            @ModelAttribute LeadFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                leadService.getList(filter, pageable)));
    }

    // Chi tiết lead
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leadService.getById(id)));
    }

    // Tạo lead
    @PostMapping
    public ResponseEntity<ApiResponse<LeadResponse>> create(
            @Valid @RequestBody CreateLeadRequest req,
            HttpServletRequest httpReq) {
        Long userId = (Long) httpReq.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(leadService.create(req, userId)));
    }

    // Cập nhật lead
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeadRequest req,
            HttpServletRequest httpReq) {
        Long userId = (Long) httpReq.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.success(
                leadService.update(id, req, userId)));
    }

    // Phân công lead
    @PatchMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<LeadResponse>> assign(
            @PathVariable Long id,
            @Valid @RequestBody AssignLeadRequest req,
            HttpServletRequest httpReq) {
        Long userId = (Long) httpReq.getAttribute("userId");
        return ResponseEntity.ok(ApiResponse.success(
                leadService.assign(id, req, userId)));
    }

    // Xóa mềm
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            HttpServletRequest httpReq) {
        Long userId = (Long) httpReq.getAttribute("userId");
        leadService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // Lịch sử thay đổi
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<LeadHistoryResponse>>> getHistory(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leadService.getHistory(id)));
    }
}