package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductDeletionPolicy {
    private final List<ProductDeletionChecker> checkers;

    /**
     * Chạy kiểm tra qua tất cả các Checker (được inject từ các module khác) để xem Product có được phép xóa không.
     * Nếu có bất kỳ Checker nào báo đang dùng (Optional trả về có giá trị), thu thập lại lý do và ném lỗi IN_USE_REASONS.
     */
    public void validateProductDeletion(Long productId) {
        List<String> reasons = checkers.stream()
                .map(checker -> checker.checkProduct(productId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (!reasons.isEmpty()) {
            throw BusinessException.of(ErrorCode.IN_USE_REASONS, "label.product", String.join(", ", reasons));
        }
    }

    /**
     * Tương tự validateProductDeletion, nhưng dùng để kiểm tra cấp độ Variant.
     * Bất kỳ module nào đang tham chiếu đến variant này đều sẽ phát hiện và chặn lại.
     */
    public void validateVariantDeletion(Long variantId) {
        List<String> reasons = checkers.stream()
                .map(checker -> checker.checkVariant(variantId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if (!reasons.isEmpty()) {
            throw BusinessException.of(ErrorCode.IN_USE_REASONS, "label.product.variant", String.join(", ", reasons));
        }
    }
}
