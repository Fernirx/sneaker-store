package com.fernirx.sneakerapi.returns.repository;

import com.fernirx.sneakerapi.returns.dto.request.ReturnFilterRequest;
import com.fernirx.sneakerapi.returns.entity.ReturnRequest;
import com.fernirx.sneakerapi.returns.enums.ReturnResolutionType;
import com.fernirx.sneakerapi.returns.enums.ReturnStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class ReturnRequestSpec {

    public static Specification<ReturnRequest> build(ReturnFilterRequest filter) {
        return Specification
                .where(hasKeyword(filter.search()))
                .and(hasResolutionType(filter.resolutionType()))
                .and(hasStatus(filter.status()))
                .and(fromDate(filter.fromDate()))
                .and(toDate(filter.toDate()));
    }

    private static Specification<ReturnRequest> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.get("order").get("code")), pattern)
            );
        };
    }

    private static Specification<ReturnRequest> hasResolutionType(ReturnResolutionType resolutionType) {
        return (root, query, cb) -> resolutionType == null ? null : cb.equal(root.get("resolutionType"), resolutionType);
    }

    private static Specification<ReturnRequest> hasStatus(ReturnStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<ReturnRequest> fromDate(LocalDateTime fromDate) {
        return (root, query, cb) -> fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    private static Specification<ReturnRequest> toDate(LocalDateTime toDate) {
        return (root, query, cb) -> toDate == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }
}
