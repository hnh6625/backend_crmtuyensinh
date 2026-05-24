package com.company.crm_backend.User.application.dto;


import com.company.crm_backend.User.domain.User;
import com.company.crm_backend.User.domain.UserStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {

    public static Specification<User> build(UserFilterRequest f) {
        return Specification
                .where(notDeleted())
                .and(byKeyword(f.getKeyword()))
                .and(byRole(f.getRole()))
                .and(byStatus(f.getStatus()));
    }

    private static Specification<User> notDeleted() {
        return (root, q, cb) -> cb.isNull(root.get("deletedAt"));
    }

    private static Specification<User> byKeyword(String keyword) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String like = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("username")), like),
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(root.get("email")),    like)
            );
        };
    }

    private static Specification<User> byRole(String role) {
        return (root, q, cb) -> !StringUtils.hasText(role) ? null
                : cb.equal(root.get("role").get("roleName"), role);
    }

    private static Specification<User> byStatus(UserStatus status) {
        return (root, q, cb) -> status == null ? null
                : cb.equal(root.get("status"), status);
    }
}