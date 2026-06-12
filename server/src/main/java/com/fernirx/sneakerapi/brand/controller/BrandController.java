package com.fernirx.sneakerapi.brand.controller;

import com.fernirx.sneakerapi.brand.dto.request.BrandFilterRequest;
import com.fernirx.sneakerapi.brand.dto.response.BrandResponse;
import com.fernirx.sneakerapi.brand.service.BrandService;
import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
}
