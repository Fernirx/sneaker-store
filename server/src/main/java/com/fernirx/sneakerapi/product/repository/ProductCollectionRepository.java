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
}
