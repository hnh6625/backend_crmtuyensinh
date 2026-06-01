package com.company.crm_backend.importjob.controller;

import com.company.crm_backend.importjob.application.ImportService;
import com.company.crm_backend.importjob.application.dto.ImportStatusResponse;
import com.company.crm_backend.importjob.domain.ImportDetail;
import com.company.crm_backend.shared.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    //  1. Upload CSV → trigger batch
    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImportStatusResponse>> upload(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest req) {
        Long userId = (Long) req.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(importService.upload(file, userId)));
    }

    // 2. Polling trạng thái — Vue gọi mỗi 2 giây
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ImportStatusResponse>> getStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.getStatus(id)));
    }

    // 3. Lịch sử import
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ImportStatusResponse>>> getHistory(
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.getHistory(pageable)));
    }

    // 4. Danh sách row lỗi
    @GetMapping("/{id}/errors")
    public ResponseEntity<ApiResponse<List<ImportDetail>>> getErrors(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                importService.getErrors(id)));
    }
}