package com.fernirx.sneakerapi.product.service.impl;

import com.fernirx.sneakerapi.collection.repository.CollectionRepository;
import com.fernirx.sneakerapi.product.repository.ProductCollectionRepository;
import com.fernirx.sneakerapi.product.service.ProductCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fernirx.sneakerapi.collection.entity.Collection;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.product.dto.request.AssignCollectionsRequest;
import com.fernirx.sneakerapi.product.dto.response.CollectionBriefResponse;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductCollection;
import com.fernirx.sneakerapi.product.repository.ProductRepository;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCollectionServiceImpl implements ProductCollectionService {

    private final ProductCollectionRepository productCollectionRepository;
    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CollectionBriefResponse> getCollections(Long productId) {
        findProduct(productId);
        return productCollectionRepository.findByProductIdOrderByCollectionLaunchDateDesc(productId)
                .stream()
                .map(pc -> new CollectionBriefResponse(
                        pc.getCollection().getId(),
                        pc.getCollection().getName(),
                        pc.getCollection().getSlug()
                ))
                .toList();
    }

    @Override
    public List<CollectionBriefResponse> assignCollections(Long productId, AssignCollectionsRequest request) {
        Product product = findProduct(productId);

        List<Collection> collections = collectionRepository.findAllById(request.collectionIds());
        if (collections.size() != request.collectionIds().size()) {
            throw BusinessException.notFound("label.collection");
        }

        productCollectionRepository.deleteByProductId(productId);

        List<ProductCollection> newLinks = collections.stream()
                .map(collection -> {
                    ProductCollection pc = new ProductCollection();
                    pc.setProduct(product);
                    pc.setCollection(collection);
                    return pc;
                })
                .toList();
        productCollectionRepository.saveAll(newLinks);

        return collections.stream()
                .map(c -> new CollectionBriefResponse(c.getId(), c.getName(), c.getSlug()))
                .toList();
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.notFound("label.product"));
    }
}
