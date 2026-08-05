package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.product.dto.request.AssignCollectionsRequest;
import com.fernirx.sneakerapi.product.dto.response.CollectionBriefResponse;

import java.util.List;

public interface ProductCollectionService {

    List<CollectionBriefResponse> getCollections(Long productId);

    List<CollectionBriefResponse> assignCollections(Long productId, AssignCollectionsRequest request);

    // Cross-module (Collection) - chuyển toàn bộ sản phẩm đang gán fromCollectionId sang toCollectionId,
    // dùng khi xóa bộ sưu tập theo lựa chọn "chuyển sang bộ sưu tập khác" thay vì gỡ hẳn.
    void reassignCollection(Long fromCollectionId, Long toCollectionId);
}
