package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.category.entity.Category;
import com.fernirx.sneakerapi.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findByProductIdOrderByCategoryDisplayOrderAsc(Long productId);

    @Modifying
    @Query("DELETE FROM ProductCategory pc WHERE pc.product.id = :productId")
    void deleteByProductId(Long productId);

    // Sản phẩm đã có sẵn cả fromCategoryId lẫn toCategoryId - xóa liên kết cũ trước khi bulk update
    // ở dưới, tránh vi phạm UNIQUE (product_id, category_id) khi 2 dòng gộp thành 1.
    @Modifying
    @Query("DELETE FROM ProductCategory pc WHERE pc.category.id = :fromCategoryId AND pc.product.id IN " +
            "(SELECT pc2.product.id FROM ProductCategory pc2 WHERE pc2.category.id = :toCategoryId)")
    void deleteDuplicatesForReassign(@Param("fromCategoryId") Long fromCategoryId, @Param("toCategoryId") Long toCategoryId);

    @Modifying
    @Query("UPDATE ProductCategory pc SET pc.category = :toCategory WHERE pc.category.id = :fromCategoryId")
    void bulkReassignCategory(@Param("fromCategoryId") Long fromCategoryId, @Param("toCategory") Category toCategory);
}
