package com.fernirx.sneakerapi.product.service;

import java.util.Optional;

public interface ProductDeletionChecker {
    /**
     * Kiểm tra xem một Product có đang được sử dụng ở module khác không.
     * @return Tên loại dữ liệu đang sử dụng (vd: "đơn hàng", "phiếu nhập") nếu có, hoặc Optional.empty() nếu an toàn.
     */
    Optional<String> checkProduct(Long productId);

    /**
     * Kiểm tra xem một Variant có đang được sử dụng ở module khác không.
     * @return Tên loại dữ liệu đang sử dụng nếu có, hoặc Optional.empty() nếu an toàn.
     */
    Optional<String> checkVariant(Long variantId);
}
