package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.product.dto.request.AssignCollectionsRequest;
import com.fernirx.sneakerapi.product.dto.response.CollectionBriefResponse;

import java.util.List;

public interface ProductCollectionService {

    List<CollectionBriefResponse> getCollections(Long productId);

    List<CollectionBriefResponse> assignCollections(Long productId, AssignCollectionsRequest request);
}
