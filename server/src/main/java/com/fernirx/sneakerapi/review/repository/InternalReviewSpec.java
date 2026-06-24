package com.fernirx.sneakerapi.review.repository;

import com.fernirx.sneakerapi.review.dto.request.InternalReviewFilterRequest;
import com.fernirx.sneakerapi.review.entity.ProductReview;
import org.springframework.data.jpa.domain.Specification;

public class InternalReviewSpec {

    public static Specification<ProductReview> build(InternalReviewFilterRequest filter) {
        return Specification
                .where(hasProduct(filter.productId()))
                .and(hasRating(filter.rating()))
                .and(isApproved(filter.approved()));
    }

    private static Specification<ProductReview> hasProduct(Long productId) {
        return (root, query, cb) -> productId == null ? null : cb.equal(root.get("product").get("id"), productId);
    }

    private static Specification<ProductReview> hasRating(Short rating) {
        return (root, query, cb) -> rating == null ? null : cb.equal(root.get("rating"), rating);
    }

    private static Specification<ProductReview> isApproved(Boolean approved) {
        return (root, query, cb) -> approved == null ? null : cb.equal(root.get("approved"), approved);
    }
}
