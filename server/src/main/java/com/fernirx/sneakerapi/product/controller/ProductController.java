package com.fernirx.sneakerapi.product.controller;

import com.fernirx.sneakerapi.product.dto.request.ProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import com.fernirx.sneakerapi.product.service.ProductService;
import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product API", description = "Sản phẩm")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Danh sách sản phẩm")
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
            @ParameterObject @ModelAttribute ProductFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(productService.getProducts(filter, pageable)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Chi tiết sản phẩm theo slug")
    public ResponseEntity<SuccessResponse<ProductDetailResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(SuccessResponse.of(productService.getBySlug(slug)));
    }
}
