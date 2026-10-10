package com.fernirx.sneakerapi.supplier.repository;

import com.fernirx.sneakerapi.supplier.dto.request.SupplierFilterRequest;
import com.fernirx.sneakerapi.supplier.entity.Supplier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SupplierSpec {

    public static Specification<Supplier> build(SupplierFilterRequest filter) {
        return Specification
                .where(hasKeyword(filter.search()))
                .and(isActive(filter.active()));
    }

    private static Specification<Supplier> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.get("name")), pattern)
            );
        };
    }

    private static Specification<Supplier> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }
}
