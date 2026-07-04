package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.product.dto.request.CreateVariantRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateVariantRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductVariantGroupResponse;
import com.fernirx.sneakerapi.product.dto.response.StockChangeResult;
import com.fernirx.sneakerapi.product.dto.response.VariantSearchResponse;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductVariantService {

    ProductVariant findActiveById(Long id);

    List<ProductVariant> findAllActiveByIds(List<Long> ids);

    ProductVariant findById(Long id);

    List<ProductVariantGroupResponse> getVariants(Long productId);

    Page<VariantSearchResponse> searchVariants(String keyword, Pageable pageable);

    ProductVariantGroupResponse.VariantResponse addVariant(Long productId, CreateVariantRequest request);

    ProductVariantGroupResponse.VariantResponse updateVariant(Long productId, Long variantId, UpdateVariantRequest request);

    void deleteVariant(Long productId, Long variantId);

    StockChangeResult decreaseStock(Long variantId, int quantity);

    StockChangeResult increaseStock(Long variantId, int quantity);
}
