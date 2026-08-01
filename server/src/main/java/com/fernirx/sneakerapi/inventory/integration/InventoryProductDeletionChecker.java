package com.fernirx.sneakerapi.inventory.integration;

import com.fernirx.sneakerapi.inventory.repository.InventoryTransactionRepository;
import com.fernirx.sneakerapi.inventory.repository.StockAdjustmentItemRepository;
import com.fernirx.sneakerapi.product.service.ProductDeletionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InventoryProductDeletionChecker implements ProductDeletionChecker {
    private final InventoryTransactionRepository transactionRepository;
    private final StockAdjustmentItemRepository adjustmentItemRepository;

    /**
     * Kiểm tra xem Product có từng phát sinh giao dịch kho (InventoryTransaction) 
     * hoặc phiếu điều chỉnh kho (StockAdjustment) nào không.
     * Trả về lý do tương ứng nếu có để ngăn việc xóa làm mất dữ liệu lịch sử.
     */
    @Override
    public Optional<String> checkProduct(Long productId) {
        if (transactionRepository.existsByVariant_Product_Id(productId)) {
            return Optional.of("lịch sử biến động kho");
        }
        if (adjustmentItemRepository.existsByVariant_Product_Id(productId)) {
            return Optional.of("phiếu điều chỉnh kho");
        }
        return Optional.empty();
    }

    /**
     * Kiểm tra xem Variant có từng phát sinh giao dịch kho hoặc phiếu điều chỉnh kho nào không.
     */
    @Override
    public Optional<String> checkVariant(Long variantId) {
        if (transactionRepository.existsByVariant_Id(variantId)) {
            return Optional.of("lịch sử biến động kho");
        }
        if (adjustmentItemRepository.existsByVariant_Id(variantId)) {
            return Optional.of("phiếu điều chỉnh kho");
        }
        return Optional.empty();
    }
}
