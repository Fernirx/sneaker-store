package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.product.dto.request.BySlugProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryService {

    Page<ProductResponse> getProductsByBrandSlug(String brandSlug, BySlugProductFilterRequest filter, Pageable pageable);

    Page<ProductResponse> getProductsByCategorySlug(String categorySlug, BySlugProductFilterRequest filter, Pageable pageable);
}
