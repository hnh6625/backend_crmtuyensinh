package com.company.crm_backend.enrollment.application.dto;

import com.company.crm_backend.enrollment.domain.Enrollment;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class EnrollmentResponse {

    private Long enrollmentId;
    private String studentCode;

    // Thông tin lead
    private Long leadId;
    private String leadName;
    private String leadPhone;
    private String leadEmail;

    // Ngành học
    private Long majorId;
    private String majorCode;
    private String majorName;

    // Cơ sở
    private Long campusId;
    private String campusName;

    // Học kỳ
    private Long semesterId;
    private String semesterCode;
    private String semesterName;

    // Học phí
    private BigDecimal tuitionFee;
    private BigDecimal scholarshipAmount;
    private BigDecimal finalFee;

    private String enrollmentStatus;
    private String conversionSource;
    private String note;
    private String enrolledByName;
    private LocalDateTime enrollmentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EnrollmentResponse from(Enrollment e) {
        return EnrollmentResponse.builder()
                .enrollmentId(e.getEnrollmentId())
                .studentCode(e.getStudentCode())
                .leadId(e.getLead().getLeadId())
                .leadName(e.getLead().getFullName())
                .leadPhone(e.getLead().getPhone())
                .leadEmail(e.getLead().getEmail())
                .majorId(e.getMajor().getMajorId())
                .majorCode(e.getMajor().getMajorCode())
                .majorName(e.getMajor().getMajorName())
                .campusId(e.getCampus().getCampusId())
                .campusName(e.getCampus().getCampusName())
                .semesterId(e.getSemester().getSemesterId())
                .semesterCode(e.getSemester().getSemesterCode())
                .semesterName(e.getSemester().getSemesterName())
                .tuitionFee(e.getTuitionFee())
                .scholarshipAmount(e.getScholarshipAmount())
                .finalFee(e.getFinalFee())
                .enrollmentStatus(e.getEnrollmentStatus().name())
                .conversionSource(e.getConversionSource())
                .note(e.getNote())
                .enrolledByName(e.getEnrolledBy() != null
                        ? e.getEnrolledBy().getFullName() : null)
                .enrollmentDate(e.getEnrollmentDate())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}