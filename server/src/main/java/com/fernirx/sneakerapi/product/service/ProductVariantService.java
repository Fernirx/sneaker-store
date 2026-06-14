package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.product.dto.request.CreateVariantRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateVariantRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductVariantGroupResponse;

import java.util.List;

public interface ProductVariantService {

    List<ProductVariantGroupResponse> getVariants(Long productId);

    ProductVariantGroupResponse.VariantResponse addVariant(Long productId, CreateVariantRequest request);

    ProductVariantGroupResponse.VariantResponse updateVariant(Long productId, Long variantId, UpdateVariantRequest request);

    void deleteVariant(Long productId, Long variantId);
}
