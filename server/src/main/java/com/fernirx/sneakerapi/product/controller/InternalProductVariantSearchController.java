package com.fernirx.sneakerapi.product.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.product.dto.response.VariantSearchResponse;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/products/variants")
@RequiredArgsConstructor
@Tag(name = "Internal Product Variant Search API", description = "Tìm kiếm biến thể sản phẩm toàn hệ thống (nội bộ)")
public class InternalProductVariantSearchController {

    private final ProductVariantService productVariantService;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Tìm kiếm variant theo SKU / tên sản phẩm / mã sản phẩm / colorway")
    public ResponseEntity<PageResponse<VariantSearchResponse>> search(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "sku") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(productVariantService.searchVariants(keyword, pageable)));
    }
}