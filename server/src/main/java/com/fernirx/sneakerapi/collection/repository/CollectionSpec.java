package com.fernirx.sneakerapi.collection.repository;

import com.fernirx.sneakerapi.collection.dto.request.CollectionFilterRequest;
import com.fernirx.sneakerapi.collection.entity.Collection;
import org.springframework.data.jpa.domain.Specification;

public class CollectionSpec {
    public static Specification<Collection> build(CollectionFilterRequest filter) {
        return Specification.where(hasKeyword(filter.search()))
                .and(isActive(filter.active()));
    }

    private static Specification<Collection> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("slug")), pattern)
            );
        };
    }

    private static Specification<Collection> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }
}
