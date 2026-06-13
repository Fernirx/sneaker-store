package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.product.dto.request.CreateProductRequest;
import com.fernirx.sneakerapi.product.dto.request.InternalProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.request.ProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateProductRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductInternalResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    // Public
    Page<ProductResponse> getProducts(ProductFilterRequest filter, Pageable pageable);
    ProductDetailResponse getBySlug(String slug);

    // Internal
    Page<ProductInternalResponse> getInternalProducts(InternalProductFilterRequest filter, Pageable pageable);
    ProductInternalResponse getInternalById(Long id);
    ProductInternalResponse createProduct(CreateProductRequest request);
    ProductInternalResponse updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
}
