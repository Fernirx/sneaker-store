package com.fernirx.sneakerapi.order.integration;

import com.fernirx.sneakerapi.order.repository.OrderItemRepository;
import com.fernirx.sneakerapi.product.service.ProductDeletionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderProductDeletionChecker implements ProductDeletionChecker {
    private final OrderItemRepository orderItemRepository;

    /**
     * Kiểm tra xem Product có đang nằm trong bất kỳ đơn hàng nào không.
     * Trả về lý do "đơn hàng" nếu tìm thấy OrderItem liên kết.
     */
    @Override
    public Optional<String> checkProduct(Long productId) {
        if (orderItemRepository.existsByVariant_Product_Id(productId)) {
            return Optional.of("đơn hàng");
        }
        return Optional.empty();
    }

    /**
     * Kiểm tra xem Variant có đang nằm trong bất kỳ đơn hàng nào không.
     * Trả về lý do "đơn hàng" nếu tìm thấy OrderItem liên kết.
     */
    @Override
    public Optional<String> checkVariant(Long variantId) {
        if (orderItemRepository.existsByVariant_Id(variantId)) {
            return Optional.of("đơn hàng");
        }
        return Optional.empty();
    }
}
