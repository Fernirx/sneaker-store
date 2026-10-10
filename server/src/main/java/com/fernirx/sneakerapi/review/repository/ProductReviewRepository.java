package com.fernirx.sneakerapi.review.repository;

import com.fernirx.sneakerapi.review.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long>, JpaSpecificationExecutor<ProductReview> {

    @Query("SELECT r FROM ProductReview r JOIN FETCH r.user u LEFT JOIN FETCH u.userProfile " +
            "WHERE r.product.id = :productId AND r.approved = true")
    Page<ProductReview> findApprovedByProductId(@Param("productId") Long productId, Pageable pageable);

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    @Query("SELECT r.rating, COUNT(r) FROM ProductReview r " +
            "WHERE r.product.id = :productId AND r.approved = true GROUP BY r.rating")
    List<Object[]> countByRatingForProduct(@Param("productId") Long productId);
}
