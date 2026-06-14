package com.fernirx.sneakerapi.brand.controller;

import com.fernirx.sneakerapi.brand.dto.request.BrandFilterRequest;
import com.fernirx.sneakerapi.brand.dto.response.BrandResponse;
import com.fernirx.sneakerapi.brand.service.BrandService;
import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.product.dto.request.BySlugProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import com.fernirx.sneakerapi.product.service.ProductQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@Tag(name = "Brand API", description = "Thương hiệu")
public class BrandController {
    private final BrandService brandService;
    private final ProductQueryService productQueryService;

    @GetMapping
    @Operation(summary = "Danh sách thương hiệu")
    public ResponseEntity<PageResponse<BrandResponse>> getBrands(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(
                brandService.getBrands(new BrandFilterRequest(search, null), pageable)
        ));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Chi tiết thương hiệu theo slug")
    public ResponseEntity<SuccessResponse<BrandResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(SuccessResponse.of(brandService.getBySlug(slug)));
    }

    @GetMapping("/{slug}/products")
    @Operation(summary = "Danh sách sản phẩm theo thương hiệu")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByBrand(
            @PathVariable String slug,
            @ParameterObject @ModelAttribute BySlugProductFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(
                productQueryService.getProductsByBrandSlug(slug, filter, pageable)
        ));
    }
}
