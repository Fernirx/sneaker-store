package com.fernirx.sneakerapi.product.service.impl;

import com.fernirx.sneakerapi.product.dto.request.BySlugProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.request.ProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import com.fernirx.sneakerapi.product.service.ProductQueryService;
import com.fernirx.sneakerapi.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductService productService;

    @Override
    public Page<ProductResponse> getProductsByBrandSlug(String brandSlug, BySlugProductFilterRequest filter, Pageable pageable) {
        ProductFilterRequest full = new ProductFilterRequest(
                filter.search(), filter.gender(), List.of(brandSlug),
                filter.minPrice(), filter.maxPrice(), filter.newArrival(), filter.onSale(),
                null
        );
        return productService.getProducts(full, pageable);
    }

    @Override
    public Page<ProductResponse> getProductsByCategorySlug(String categorySlug, BySlugProductFilterRequest filter, Pageable pageable) {
        ProductFilterRequest full = new ProductFilterRequest(
                filter.search(), filter.gender(), null,
                filter.minPrice(), filter.maxPrice(), filter.newArrival(), filter.onSale(),
                List.of(categorySlug)
        );
        return productService.getProducts(full, pageable);
    }
}
