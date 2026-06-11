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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
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

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        String csvContent = "\uFEFF" + // Ký tự BOM giúp Excel đọc tiếng Việt UTF-8 không bị lỗi font
                "Họ và tên,Số điện thoại,Email,Giới tính,Ngày sinh,Trường học,Năm tốt nghiệp,Tỉnh/Thành,Địa chỉ,Ghi chú,Nguồn\n" +
                "Nguyễn Văn A,0901234567,nva@gmail.com,MALE,01/01/2005,THPT Chuyên,2023,Hà Nội,Đống Đa,Khách VIP,Facebook\n" +
                "Trần Thị B,0987654321,ttb@gmail.com,FEMALE,15/05/2005,THPT Lê Hồng Phong,2023,Hồ Chí Minh,Quận 1,,Website\n";

        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);

        // 2. Cấu hình Header để trình duyệt hiểu đây là file cần tải xuống
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "Lead_Import_Template.csv");

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }
}