package com.company.crm_backend.enrollment.application.dto;

import com.company.crm_backend.enrollment.domain.Enrollment;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class EnrollmentResponse {
    private Long id;
    private Long leadId;
    private String leadName;
    private String leadPhone;

    private Long majorId;
    private String majorName;

    private BigDecimal tuitionFee;
    private BigDecimal scholarshipAmount;
    private BigDecimal finalFee;

    private String studentCode;
    private String paymentMethod;
    private String enrollmentStatus;
    private String note;
    private LocalDateTime createdAt;
    private String enrolledByName;

    public static EnrollmentResponse from(Enrollment e) {
        return EnrollmentResponse.builder()
                .id(e.getEnrollmentId())
                .leadId(e.getLead() != null ? e.getLead().getLeadId() : null)
                .leadName(e.getLead() != null ? e.getLead().getFullName() : null)
                .leadPhone(e.getLead() != null ? e.getLead().getPhone() : null)
                .majorId(e.getMajor() != null ? e.getMajor().getMajorId() : null)
                .majorName(e.getMajor() != null ? e.getMajor().getMajorName() : null)
                .tuitionFee(e.getTuitionFee())
                .scholarshipAmount(e.getScholarshipAmount())
                .finalFee(e.getFinalFee())
                .studentCode(e.getStudentCode())
                .paymentMethod(e.getPaymentMethod())
                .enrollmentStatus(e.getEnrollmentStatus() != null ? e.getEnrollmentStatus().name() : null)
                .note(e.getNote())
                .createdAt(e.getCreatedAt())
                .enrolledByName(e.getEnrolledBy() != null ? e.getEnrolledBy().getFullName() : null)
                .build();
    }
}