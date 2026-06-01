package com.company.crm_backend.enrollment.infrastructure;

import com.company.crm_backend.enrollment.application.dto.EnrollmentFilterRequest;
import com.company.crm_backend.enrollment.domain.Enrollment;
import com.company.crm_backend.enrollment.domain.EnrollmentStatus;
import com.company.crm_backend.lead.domain.Lead;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class EnrollmentSpecification {

    public static Specification<Enrollment> build(EnrollmentFilterRequest f) {
        return Specification
                .allOf(bySemester(f.getSemesterId()))
                .and(byMajor(f.getMajorId()))
                .and(byCampus(f.getCampusId()))
                .and(byStatus(f.getEnrollmentStatus()))
                .and(byKeyword(f.getKeyword()));
    }

    // Lọc theo học kỳ
    private static Specification<Enrollment> bySemester(Long semesterId) {
        return (root, q, cb) -> semesterId == null ? null
                : cb.equal(root.get("semester").get("semesterId"), semesterId);
    }

    // Lọc theo ngành
    private static Specification<Enrollment> byMajor(Long majorId) {
        return (root, q, cb) -> majorId == null ? null
                : cb.equal(root.get("major").get("majorId"), majorId);
    }

    // Lọc theo cơ sở
    private static Specification<Enrollment> byCampus(Long campusId) {
        return (root, q, cb) -> campusId == null ? null
                : cb.equal(root.get("campus").get("campusId"), campusId);
    }

    // Lọc theo trạng thái
    private static Specification<Enrollment> byStatus(EnrollmentStatus status) {
        return (root, q, cb) -> status == null ? null
                : cb.equal(root.get("enrollmentStatus"), status);
    }

    // Tìm theo tên / SĐT / mã sinh viên
    private static Specification<Enrollment> byKeyword(String keyword) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String like = "%" + keyword.trim().toLowerCase() + "%";
            Join<Enrollment, Lead> lead = root.join("lead", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(lead.get("fullName")),         like),
                    cb.like(lead.get("phone"),                      like),
                    cb.like(cb.lower(root.get("studentCode")),      like)
            );
        };
    }
}