package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.product.dto.request.CreateProductRequest;
import com.fernirx.sneakerapi.product.dto.request.InternalProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.request.ProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateProductRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductInternalResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import com.fernirx.sneakerapi.product.entity.Product;
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

    // Cross-module (Review)
    Product findEntityById(Long id);
    Product findActiveBySlug(String slug);

    // Cross-module (Brand) - chuyển toàn bộ sản phẩm đang gán fromBrandId sang toBrandId, dùng khi xóa
    // thương hiệu theo lựa chọn "chuyển sang thương hiệu khác" (Brand là FK bắt buộc, không thể gỡ hẳn).
    void reassignBrand(Long fromBrandId, Long toBrandId);
}
