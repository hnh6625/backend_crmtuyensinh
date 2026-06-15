package com.company.crm_backend.enrollment.infrastructure;

import com.company.crm_backend.enrollment.application.dto.EnrollmentFilterRequest;
import com.company.crm_backend.enrollment.domain.Enrollment;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class EnrollmentSpecification {
    public static Specification<Enrollment> build(EnrollmentFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.getKeyword())) {
                String kw = "%" + filter.getKeyword().toLowerCase() + "%";
                Predicate matchName = cb.like(cb.lower(root.join("lead").get("fullName")), kw);
                Predicate matchPhone = cb.like(root.join("lead").get("phone"), kw);
                predicates.add(cb.or(matchName, matchPhone));
            }

            if (filter.getMajorId() != null) {
                predicates.add(cb.equal(root.join("major").get("majorId"), filter.getMajorId()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("enrollmentStatus"), filter.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}