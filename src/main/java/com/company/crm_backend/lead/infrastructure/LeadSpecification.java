package com.company.crm_backend.lead.infrastructure;

import com.company.crm_backend.lead.domain.Lead;
import com.company.crm_backend.lead.domain.dto.LeadFilterRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class LeadSpecification {

    // Build tất cả điều kiện filter
    public static Specification<Lead> build(LeadFilterRequest f) {
        return Specification
                .allOf(notDeleted())
                .and(byKeyword(f.getKeyword()))
                .and(byStatus(f.getStatusId()))
                .and(bySource(f.getSourceId()))
                .and(byAssignedTo(f.getAssignedTo()))
                .and(byProvince(f.getProvince()))
                .and(byCreatedFrom(f.getCreatedFrom()))
                .and(byCreatedTo(f.getCreatedTo()));
    }

    // Chỉ lấy lead chưa bị soft delete
    private static Specification<Lead> notDeleted() {
        return (root, q, cb) -> cb.isNull(root.get("deletedAt"));
    }

    // Tìm theo tên / SĐT / email
    private static Specification<Lead> byKeyword(String keyword) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String like = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")),        like),
                    cb.like(root.get("phone"),                     like),
                    cb.like(root.get("phoneNormalized"),           like),
                    cb.like(cb.lower(root.get("email")),           like)
            );
        };
    }

    // ── Lọc theo trạng thái
    private static Specification<Lead> byStatus(Long statusId) {
        return (root, q, cb) -> statusId == null ? null
                : cb.equal(root.get("status").get("statusId"), statusId);
    }

    // ── Lọc theo nguồn
    private static Specification<Lead> bySource(Long sourceId) {
        return (root, q, cb) -> sourceId == null ? null
                : cb.equal(root.get("source").get("sourceId"), sourceId);
    }

    // Lọc theo người phụ trách
    private static Specification<Lead> byAssignedTo(Long userId) {
        return (root, q, cb) -> userId == null ? null
                : cb.equal(root.get("assignedTo").get("userId"), userId);
    }

    // Lọc theo tỉnh thành
    private static Specification<Lead> byProvince(String province) {
        return (root, q, cb) -> !StringUtils.hasText(province) ? null
                : cb.like(cb.lower(root.get("province")),
                "%" + province.trim().toLowerCase() + "%");
    }

    // Lọc theo ngày tạo từ
    private static Specification<Lead> byCreatedFrom(LocalDateTime from) {
        return (root, q, cb) -> from == null ? null
                : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    // Lọc theo ngày tạo đến
    private static Specification<Lead> byCreatedTo(LocalDateTime to) {
        return (root, q, cb) -> to == null ? null
                : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}