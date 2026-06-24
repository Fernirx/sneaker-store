package com.fernirx.sneakerapi.review.repository;

import com.fernirx.sneakerapi.review.dto.request.InternalCommentFilterRequest;
import com.fernirx.sneakerapi.review.entity.ProductComment;
import org.springframework.data.jpa.domain.Specification;

public class InternalCommentSpec {

    public static Specification<ProductComment> build(InternalCommentFilterRequest filter) {
        return Specification
                .where(hasProduct(filter.productId()))
                .and(isApproved(filter.approved()));
    }

    private static Specification<ProductComment> hasProduct(Long productId) {
        return (root, query, cb) -> productId == null ? null : cb.equal(root.get("product").get("id"), productId);
    }

    private static Specification<ProductComment> isApproved(Boolean approved) {
        return (root, query, cb) -> approved == null ? null : cb.equal(root.get("approved"), approved);
    }
}
