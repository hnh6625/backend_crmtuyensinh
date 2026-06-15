package com.company.crm_backend.enrollment.controller;

import com.company.crm_backend.enrollment.application.EnrollmentService;
import com.company.crm_backend.enrollment.application.dto.CreateEnrollmentRequest;
import com.company.crm_backend.enrollment.application.dto.EnrollmentFilterRequest;
import com.company.crm_backend.enrollment.application.dto.UpdateEnrollmentRequest;
import com.company.crm_backend.enrollment.domain.EnrollmentStatus;
import com.company.crm_backend.shared.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/majors")
    public ResponseEntity<?> getMajors() {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.getActiveMajors()));
    }

    @GetMapping
    public ResponseEntity<?> getList(@ModelAttribute EnrollmentFilterRequest filter,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                enrollmentService.getList(filter, org.springframework.data.domain.PageRequest.of(page, size))
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.getById(id)));
    }

    @GetMapping("/by-lead/{leadId}")
    public ResponseEntity<?> getByLead(@PathVariable Long leadId) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.getByLeadId(leadId)));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateEnrollmentRequest req,
                                    @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.create(req, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateEnrollmentRequest req) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.update(id, req)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestParam EnrollmentStatus status) {
        return ResponseEntity.ok(ApiResponse.success(enrollmentService.updateStatus(id, status)));
    }
}