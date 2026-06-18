package com.fernirx.sneakerapi.inventory.service;

import com.fernirx.sneakerapi.inventory.enums.InventoryReferenceType;
import com.fernirx.sneakerapi.inventory.enums.InventoryTransactionType;

public interface InventoryTransactionService {

    void record(Long variantId, Long userId, InventoryTransactionType type, int quantity,
                 int oldStock, int newStock, InventoryReferenceType referenceType,
                 Long referenceId, String note);
}
