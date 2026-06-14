package com.fernirx.sneakerapi.brand.repository;

import com.fernirx.sneakerapi.brand.dto.request.BrandFilterRequest;
import com.fernirx.sneakerapi.brand.entity.Brand;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class BrandSpec {

    public static Specification<Brand> build(BrandFilterRequest filter) {
        return Specification
                .where(hasKeyword(filter.search()))
                .and(isActive(filter.active()));
    }

    private static Specification<Brand> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("slug")), pattern)
            );
        };
    }

    private static Specification<Brand> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }
}
