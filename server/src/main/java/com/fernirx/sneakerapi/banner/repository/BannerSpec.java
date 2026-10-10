package com.fernirx.sneakerapi.banner.repository;

import com.fernirx.sneakerapi.banner.dto.request.BannerFilterRequest;
import com.fernirx.sneakerapi.banner.entity.Banner;
import org.springframework.data.jpa.domain.Specification;

public class BannerSpec {
    public static Specification<Banner> build(BannerFilterRequest filter) {
        return Specification.where(hasKeyword(filter.search()))
                .and(isActive(filter.active()));
    }

    private static Specification<Banner> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("title")), pattern);
        };
    }

    private static Specification<Banner> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }
}
