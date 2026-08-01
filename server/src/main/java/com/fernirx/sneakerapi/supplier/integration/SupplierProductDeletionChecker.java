package com.fernirx.sneakerapi.supplier.integration;

import com.fernirx.sneakerapi.product.service.ProductDeletionChecker;
import com.fernirx.sneakerapi.supplier.repository.PurchaseItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SupplierProductDeletionChecker implements ProductDeletionChecker {
    private final PurchaseItemRepository purchaseItemRepository;

    /**
     * Kiểm tra xem Product có từng được tạo phiếu nhập hàng (PurchaseItem) nào không.
     * Trả về lý do "phiếu nhập hàng" nếu tìm thấy để ngăn xóa.
     */
    @Override
    public Optional<String> checkProduct(Long productId) {
        if (purchaseItemRepository.existsByVariant_Product_Id(productId)) {
            return Optional.of("phiếu nhập hàng");
        }
        return Optional.empty();
    }

    /**
     * Kiểm tra xem Variant có từng được tạo phiếu nhập hàng nào không.
     */
    @Override
    public Optional<String> checkVariant(Long variantId) {
        if (purchaseItemRepository.existsByVariant_Id(variantId)) {
            return Optional.of("phiếu nhập hàng");
        }
        return Optional.empty();
    }
}
