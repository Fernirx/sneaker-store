package com.fernirx.sneakerapi.collection.service.impl;

import com.fernirx.sneakerapi.collection.dto.request.CollectionFilterRequest;
import com.fernirx.sneakerapi.collection.dto.request.CreateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.request.TranslationRequest;
import com.fernirx.sneakerapi.collection.dto.request.UpdateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.response.CollectionInternalResponse;
import com.fernirx.sneakerapi.collection.dto.response.CollectionResponse;
import com.fernirx.sneakerapi.collection.entity.Collection;
import com.fernirx.sneakerapi.collection.entity.CollectionTranslation;
import com.fernirx.sneakerapi.collection.mapper.CollectionMapper;
import com.fernirx.sneakerapi.collection.repository.CollectionRepository;
import com.fernirx.sneakerapi.collection.repository.CollectionSpec;
import com.fernirx.sneakerapi.collection.repository.CollectionTranslationRepository;
import com.fernirx.sneakerapi.collection.service.CollectionService;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;
    private final CollectionTranslationRepository collectionTranslationRepository;
    private final CollectionMapper collectionMapper;
    private final Slugify slugify;

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
        collection.setDescription(request.description());
        collection.setImagePublicId(request.imagePublicId());
        collection.setLaunchDate(request.launchDate());
        collection.setEndDate(request.endDate());
        collection.setActive(true);
        Collection saved = collectionRepository.save(collection);

        if (request.translations() != null) {
            saveTranslations(saved, request.translations());
        }
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
        collectionRepository.save(collection);
        if (request.translations() != null) {
            collectionTranslationRepository.deleteByCollectionId(id);
            saveTranslations(collection, request.translations());
        }
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
    public void deleteCollection(Long id) {
        collectionRepository.delete(findById(id));
    }

    private void saveTranslations(Collection collection, List<TranslationRequest> translations) {
        long distinctCount = translations.stream()
                .map(TranslationRequest::locale)
                .distinct()
                .count();
        if (distinctCount < translations.size()) {
            throw BusinessException.bad("label.locale");
        }
        translations.forEach(t -> {
            CollectionTranslation translation = new CollectionTranslation();
            translation.setCollection(collection);
            translation.setLocale(t.locale());
            translation.setName(t.name());
            translation.setDescription(t.description());
            collectionTranslationRepository.save(translation);
        });
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
