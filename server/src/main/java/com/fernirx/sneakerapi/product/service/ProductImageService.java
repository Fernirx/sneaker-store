package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.product.dto.request.AddImageRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateImageRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductImageGroupResponse;

import java.util.List;
import java.util.Map;

public interface ProductImageService {

    Map<String, String> getPrimaryImageMap(List<Long> productIds);

    List<ProductImageGroupResponse> getImages(Long productId);

    ProductImageGroupResponse.ImageResponse addImage(Long productId, AddImageRequest request);

    ProductImageGroupResponse.ImageResponse updateImage(Long productId, Long imageId, UpdateImageRequest request);

    void deleteImage(Long productId, Long imageId);
}
