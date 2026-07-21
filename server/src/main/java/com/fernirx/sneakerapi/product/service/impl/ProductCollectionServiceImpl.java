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

    @Override
    public void reassignCollection(Long fromCollectionId, Long toCollectionId) {
        productCollectionRepository.deleteDuplicatesForReassign(fromCollectionId, toCollectionId);
        productCollectionRepository.bulkReassignCollection(fromCollectionId, collectionRepository.getReferenceById(toCollectionId));
    }
}
