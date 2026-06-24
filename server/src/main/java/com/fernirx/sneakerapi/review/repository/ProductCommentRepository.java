package com.fernirx.sneakerapi.review.repository;

import com.fernirx.sneakerapi.review.entity.ProductComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductCommentRepository extends JpaRepository<ProductComment, Long>, JpaSpecificationExecutor<ProductComment> {

    @Query("SELECT c FROM ProductComment c JOIN FETCH c.user u LEFT JOIN FETCH u.userProfile " +
            "WHERE c.product.id = :productId ORDER BY c.createdAt ASC")
    List<ProductComment> findAllByProductId(@Param("productId") Long productId);
}
