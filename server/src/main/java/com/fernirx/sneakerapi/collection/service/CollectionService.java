package com.fernirx.sneakerapi.collection.service;

import com.fernirx.sneakerapi.collection.dto.request.CollectionFilterRequest;
import com.fernirx.sneakerapi.collection.dto.request.CreateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.request.UpdateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.response.CollectionInternalResponse;
import com.fernirx.sneakerapi.collection.dto.response.CollectionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CollectionService {
    Page<CollectionResponse> getCollections(CollectionFilterRequest filter, Pageable pageable);
    CollectionResponse getBySlug(String slug);
    Page<CollectionInternalResponse> getInternalCollections(CollectionFilterRequest filter, Pageable pageable);
    CollectionInternalResponse getInternalById(Long id);
    CollectionInternalResponse createCollection(CreateCollectionRequest request);
    CollectionInternalResponse updateCollection(Long id, UpdateCollectionRequest request);
    CollectionInternalResponse updateCollectionSlug(Long id, String slug);
    void deleteCollection(Long id);
}
