package com.fernirx.sneakerapi.product.repository;

import com.fernirx.sneakerapi.collection.entity.Collection;
import com.fernirx.sneakerapi.product.entity.ProductCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductCollectionRepository extends JpaRepository<ProductCollection, Long> {

    List<ProductCollection> findByProductIdOrderByCollectionLaunchDateDesc(Long productId);

    @Modifying
    @Query("DELETE FROM ProductCollection pc WHERE pc.product.id = :productId")
    void deleteByProductId(Long productId);

    // Sản phẩm đã có sẵn cả fromCollectionId lẫn toCollectionId - xóa liên kết cũ trước khi bulk update
    // ở dưới, tránh vi phạm UNIQUE (product_id, collection_id) khi 2 dòng gộp thành 1.
    @Query("SELECT pc.product.id FROM ProductCollection pc WHERE pc.collection.id = :collectionId")
    List<Long> findProductIdsByCollectionId(@Param("collectionId") Long collectionId);

    @Modifying
    @Query("DELETE FROM ProductCollection pc WHERE pc.collection.id = :collectionId AND pc.product.id IN :productIds")
    void deleteByCollectionAndProductIds(@Param("collectionId") Long collectionId, @Param("productIds") List<Long> productIds);

    @Modifying
    @Query("UPDATE ProductCollection pc SET pc.collection = :toCollection WHERE pc.collection.id = :fromCollectionId")
    void bulkReassignCollection(@Param("fromCollectionId") Long fromCollectionId, @Param("toCollection") Collection toCollection);
}
