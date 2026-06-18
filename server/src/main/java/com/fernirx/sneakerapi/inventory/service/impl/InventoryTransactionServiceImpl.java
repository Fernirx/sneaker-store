package com.fernirx.sneakerapi.inventory.service.impl;

import com.fernirx.sneakerapi.inventory.entity.InventoryTransaction;
import com.fernirx.sneakerapi.inventory.enums.InventoryReferenceType;
import com.fernirx.sneakerapi.inventory.enums.InventoryTransactionType;
import com.fernirx.sneakerapi.inventory.repository.InventoryTransactionRepository;
import com.fernirx.sneakerapi.inventory.service.InventoryTransactionService;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryTransactionServiceImpl implements InventoryTransactionService {
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void record(Long variantId, Long userId, InventoryTransactionType type, int quantity,
                        int oldStock, int newStock, InventoryReferenceType referenceType,
                        Long referenceId, String note) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setVariant(entityManager.getReference(ProductVariant.class, variantId));
        transaction.setCreatedBy(userId != null ? entityManager.getReference(User.class, userId) : null);
        transaction.setType(type);
        transaction.setQuantity(quantity);
        transaction.setOldStock(oldStock);
        transaction.setNewStock(newStock);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setNote(note);
        inventoryTransactionRepository.save(transaction);
    }
}
