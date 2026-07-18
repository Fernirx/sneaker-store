package com.fernirx.sneakerapi.returns.dto.response;

import com.fernirx.sneakerapi.product.enums.ShoeWidth;

import java.math.BigDecimal;
import java.util.List;

public record EligibleOrderItemResponse(
        Long orderItemId,
        String productName,
        String variantSku,
        Short variantSize,
        String variantColor,
        BigDecimal unitPrice,
        Integer purchasedQuantity,
        Integer maxReturnableQuantity,
        List<ExchangeCandidate> exchangeCandidates
) {
    /** Chỉ liệt kê variant khác cùng sản phẩm, đang active và CÙNG GIÁ với variant gốc (v1 không xử lý chênh lệch giá khi đổi hàng). */
    public record ExchangeCandidate(
            Long variantId,
            Short size,
            ShoeWidth shoeWidth,
            String colorway,
            String sku,
            BigDecimal price,
            Integer stockQuantity
    ) {}
}
