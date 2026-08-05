package com.fernirx.sneakerapi.product.service.impl;

import com.fernirx.sneakerapi.collection.repository.CollectionRepository;
import com.fernirx.sneakerapi.product.repository.ProductCollectionRepository;
import com.fernirx.sneakerapi.product.service.ProductCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCollectionServiceImpl implements ProductCollectionService {

    private final ProductCollectionRepository productCollectionRepository;
    private final CollectionRepository collectionRepository;

    /**
     * Chuyển tất cả sản phẩm từ bộ sưu tập cũ sang bộ sưu tập mới (dùng khi gộp hoặc xóa Collection).
     * Tương tự Category, xóa liên kết trùng lặp trước khi bulk update để chống lỗi Unique Constraint.
     */
    @Override
    public void reassignCollection(Long fromCollectionId, Long toCollectionId) {
        java.util.List<Long> duplicateProductIds = productCollectionRepository.findProductIdsByCollectionId(toCollectionId);
        if (!duplicateProductIds.isEmpty()) {
            productCollectionRepository.deleteByCollectionAndProductIds(fromCollectionId, duplicateProductIds);
        }
        productCollectionRepository.bulkReassignCollection(fromCollectionId, collectionRepository.getReferenceById(toCollectionId));
    }
}
