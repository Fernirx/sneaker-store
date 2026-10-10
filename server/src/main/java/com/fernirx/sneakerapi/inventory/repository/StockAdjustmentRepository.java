package com.fernirx.sneakerapi.inventory.repository;

import com.fernirx.sneakerapi.inventory.entity.StockAdjustment;
import com.fernirx.sneakerapi.inventory.enums.StockAdjustmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long>, JpaSpecificationExecutor<StockAdjustment> {
    long countByStatus(StockAdjustmentStatus status);
}
