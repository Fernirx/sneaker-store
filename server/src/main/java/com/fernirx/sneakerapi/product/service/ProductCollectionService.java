package com.fernirx.sneakerapi.product.service;

public interface ProductCollectionService {

    // Cross-module (Collection) - chuyển toàn bộ sản phẩm đang gán fromCollectionId sang toCollectionId,
    // dùng khi xóa bộ sưu tập theo lựa chọn "chuyển sang bộ sưu tập khác" thay vì gỡ hẳn.
    void reassignCollection(Long fromCollectionId, Long toCollectionId);
}
