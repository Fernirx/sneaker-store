package com.fernirx.sneakerapi.category.repository;

import com.fernirx.sneakerapi.category.dto.request.CategoryFilterRequest;
import com.fernirx.sneakerapi.category.entity.Category;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CategorySpec {

    public static Specification<Category> build(CategoryFilterRequest filter) {
        return Specification
                .where(hasKeyword(filter.search()))
                .and(isActive(filter.active()))
                .and(hasParent(filter.parentId()));
    }

    private static Specification<Category> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("slug")), pattern)
            );
        };
    }

    private static Specification<Category> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    private static Specification<Category> hasParent(Long parentId) {
        return (root, query, cb) -> parentId == null ? null
                : cb.equal(root.get("parent").get("id"), parentId);
    }
}
