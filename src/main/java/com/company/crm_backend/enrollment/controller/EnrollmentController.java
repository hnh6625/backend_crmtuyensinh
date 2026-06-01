package com.company.crm_backend.enrollment.controller;

import com.company.crm_backend.enrollment.application.EnrollmentService;
import com.company.crm_backend.enrollment.application.dto.CreateEnrollmentRequest;
import com.company.crm_backend.enrollment.application.dto.EnrollmentFilterRequest;
import com.company.crm_backend.enrollment.application.dto.EnrollmentResponse;
import com.company.crm_backend.enrollment.application.dto.UpdateEnrollmentRequest;
import com.company.crm_backend.enrollment.domain.Campus;
import com.company.crm_backend.enrollment.domain.EnrollmentStatus;
import com.company.crm_backend.enrollment.domain.Major;
import com.company.crm_backend.enrollment.domain.Semester;
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
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // Dropdown
    @GetMapping("/majors")
    public ResponseEntity<ApiResponse<List<Major>>> getMajors() {
        return ResponseEntity.ok(
                ApiResponse.success(enrollmentService.getActiveMajors()));
    }

    @GetMapping("/campuses")
    public ResponseEntity<ApiResponse<List<Campus>>> getCampuses() {
        return ResponseEntity.ok(
                ApiResponse.success(enrollmentService.getActiveCampuses()));
    }

    @GetMapping("/semesters")
    public ResponseEntity<ApiResponse<List<Semester>>> getSemesters() {
        return ResponseEntity.ok(
                ApiResponse.success(enrollmentService.getOpenSemesters()));
    }

    // Danh sách
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> getList(
            @ModelAttribute EnrollmentFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.getList(filter, pageable)));
    }

    // Chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.getById(id)));
    }

    // Lấy theo lead
    @GetMapping("/by-lead/{leadId}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getByLead(
            @PathVariable Long leadId) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.getByLeadId(leadId)));
    }

    // Tạo enrollment
    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> create(
            @Valid @RequestBody CreateEnrollmentRequest req,
            HttpServletRequest httpReq) {
        Long userId = (Long) httpReq.getAttribute("userId");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(enrollmentService.create(req, userId)));
    }

    // Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEnrollmentRequest req) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.update(id, req)));
    }

    // Cập nhật trạng thái
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam EnrollmentStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.updateStatus(id, status)));
    }
}