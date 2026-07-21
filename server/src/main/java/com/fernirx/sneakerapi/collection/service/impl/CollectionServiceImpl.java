package com.fernirx.sneakerapi.collection.service.impl;

import com.fernirx.sneakerapi.collection.dto.request.CollectionFilterRequest;
import com.fernirx.sneakerapi.collection.dto.request.CreateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.request.UpdateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.response.CollectionInternalResponse;
import com.fernirx.sneakerapi.collection.dto.response.CollectionResponse;
import com.fernirx.sneakerapi.collection.entity.Collection;
import com.fernirx.sneakerapi.collection.mapper.CollectionMapper;
import com.fernirx.sneakerapi.collection.repository.CollectionRepository;
import com.fernirx.sneakerapi.collection.repository.CollectionSpec;
import com.fernirx.sneakerapi.collection.service.CollectionService;
import com.fernirx.sneakerapi.product.service.ProductCollectionService;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.owasp.html.PolicyFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;
    private final ProductCollectionService productCollectionService;
    private final Slugify slugify;
    private final PolicyFactory richTextHtmlPolicy;

    @Override
    @Transactional(readOnly = true)
    public Page<CollectionResponse> getCollections(CollectionFilterRequest filter, Pageable pageable) {
        CollectionFilterRequest publicFilter = new CollectionFilterRequest(filter.search(), true);
        return collectionRepository.findAll(CollectionSpec.build(publicFilter), pageable)
                .map(collectionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionResponse getBySlug(String slug) {
        Collection collection = collectionRepository.findBySlug(slug)
                .filter(Collection::getActive)
                .orElseThrow(() -> BusinessException.notFound("label.collection"));
        return collectionMapper.toResponse(collection);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollectionInternalResponse> getInternalCollections(CollectionFilterRequest filter, Pageable pageable) {
        return collectionRepository.findAll(CollectionSpec.build(filter), pageable)
                .map(collectionMapper::toInternalResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionInternalResponse getInternalById(Long id) {
        return collectionMapper.toInternalResponse(findById(id));
    }

    @Override
    public CollectionInternalResponse createCollection(CreateCollectionRequest request) {
        if (collectionRepository.existsByNameIgnoreCase(request.name())) {
            throw BusinessException.alreadyExists("label.collection");
        }
        String slug = generateUniqueSlug(request.name());

        Collection collection = new Collection();
        collection.setName(request.name());
        collection.setSlug(slug);
        collection.setDescription(request.description() != null ? richTextHtmlPolicy.sanitize(request.description()) : null);
        collection.setImagePublicId(request.imagePublicId());
        collection.setLaunchDate(request.launchDate());
        collection.setEndDate(request.endDate());
        collection.setActive(true);
        Collection saved = collectionRepository.save(collection);
        return collectionMapper.toInternalResponse(collectionRepository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public CollectionInternalResponse updateCollection(Long id, UpdateCollectionRequest request) {
        Collection collection = findById(id);
        if (request.name() != null && !request.name().equalsIgnoreCase(collection.getName())) {
            if (collectionRepository.existsByNameIgnoreCase(request.name())) {
                throw BusinessException.alreadyExists("label.collection");
            }
        }
        collectionMapper.updateCollection(request, collection);
        if (request.description() != null) {
            collection.setDescription(richTextHtmlPolicy.sanitize(request.description()));
        }
        collectionRepository.save(collection);
        return collectionMapper.toInternalResponse(collectionRepository.findById(id).orElseThrow());
    }

    @Override
    public CollectionInternalResponse updateCollectionSlug(Long id, String slug) {
        Collection collection = findById(id);
        if (!slug.equals(collection.getSlug()) && collectionRepository.existsBySlug(slug)) {
            throw BusinessException.alreadyExists("label.slug");
        }
        collection.setSlug(slug);
        return collectionMapper.toInternalResponse(collectionRepository.save(collection));
    }

    @Override
    public void reassignAndDelete(Long id, Long reassignToId) {
        Collection collection = findById(id);
        if (reassignToId != null) {
            findById(reassignToId); // validate đích
            productCollectionService.reassignCollection(id, reassignToId);
        } else if (!collection.getProductCollections().isEmpty()) {
            throw BusinessException.inUse("label.collection");
        }
        collectionRepository.delete(collection);
    }

    private String generateUniqueSlug(String name) {
        String base = slugify.slugify(name);
        if (!collectionRepository.existsBySlug(base)) {
            return base;
        }
        int suffix = 1;
        String candidate;
        do {
            candidate = base + "-" + suffix++;
        } while (collectionRepository.existsBySlug(candidate));
        return candidate;
    }

    private Collection findById(Long id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.collection"));
    }
}
