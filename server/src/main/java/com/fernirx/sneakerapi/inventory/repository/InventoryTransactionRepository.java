package com.fernirx.sneakerapi.inventory.repository;

import com.fernirx.sneakerapi.inventory.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
}