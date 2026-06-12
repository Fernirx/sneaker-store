package com.fernirx.sneakerapi.brand.controller;

import com.fernirx.sneakerapi.brand.dto.request.BrandFilterRequest;
import com.fernirx.sneakerapi.brand.dto.request.CreateBrandRequest;
import com.fernirx.sneakerapi.brand.dto.request.UpdateBrandRequest;
import com.fernirx.sneakerapi.brand.dto.response.BrandInternalResponse;
import com.fernirx.sneakerapi.brand.service.BrandService;
import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/brands")
@RequiredArgsConstructor
@Tag(name = "Internal Brand API", description = "Quản lý thương hiệu (nội bộ)")
public class InternalBrandController {
    private final BrandService brandService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'MARKETING')")
    @Operation(summary = "Danh sách thương hiệu")
    public ResponseEntity<PageResponse<BrandInternalResponse>> getInternalBrands(
            @ParameterObject @ModelAttribute BrandFilterRequest filter,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(brandService.getInternalBrands(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'MARKETING')")
    @Operation(summary = "Chi tiết thương hiệu")
    public ResponseEntity<SuccessResponse<BrandInternalResponse>> getInternalById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(brandService.getInternalById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo thương hiệu mới")
    public ResponseEntity<SuccessResponse<BrandInternalResponse>> createBrand(
            @Valid @RequestBody CreateBrandRequest request) {
        BrandInternalResponse response = brandService.createBrand(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.brand")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Cập nhật thương hiệu")
    public ResponseEntity<SuccessResponse<BrandInternalResponse>> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBrandRequest request) {
        BrandInternalResponse response = brandService.updateBrand(id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.brand")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa thương hiệu")
    public ResponseEntity<SuccessResponse<Void>> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.brand"))
        ));
    }
}
