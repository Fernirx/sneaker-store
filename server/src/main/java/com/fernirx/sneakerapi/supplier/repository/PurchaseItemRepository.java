package com.fernirx.sneakerapi.supplier.repository;

import com.fernirx.sneakerapi.supplier.entity.Purchase;
import com.fernirx.sneakerapi.supplier.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
    List<PurchaseItem> findAllByPurchase(Purchase purchase);
    boolean existsByVariant_Product_Id(Long productId);
    boolean existsByVariant_Id(Long variantId);
}