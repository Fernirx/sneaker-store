package com.fernirx.sneakerapi.inventory.repository;

import com.fernirx.sneakerapi.inventory.entity.StockAdjustment;
import com.fernirx.sneakerapi.inventory.entity.StockAdjustmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockAdjustmentItemRepository extends JpaRepository<StockAdjustmentItem, Long> {
    List<StockAdjustmentItem> findAllByAdjustment(StockAdjustment adjustment);
    boolean existsByVariant_Product_Id(Long productId);
    boolean existsByVariant_Id(Long variantId);
}
