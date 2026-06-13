package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.product.dto.request.ProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<ProductResponse> getProducts(ProductFilterRequest filter, Pageable pageable);

    ProductDetailResponse getBySlug(String slug);
}
