package com.fernirx.sneakerapi.product.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.product.dto.request.CreateVariantRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateVariantRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductVariantGroupResponse;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/products/{productId}/variants")
@RequiredArgsConstructor
@Tag(name = "Internal Product Variant API", description = "Quản lý biến thể sản phẩm (nội bộ)")
public class InternalProductVariantController {

    private final ProductVariantService productVariantService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Danh sách variant theo màu")
    public ResponseEntity<SuccessResponse<List<ProductVariantGroupResponse>>> getVariants(
            @PathVariable Long productId) {
        return ResponseEntity.ok(SuccessResponse.of(productVariantService.getVariants(productId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm variant mới")
    public ResponseEntity<SuccessResponse<ProductVariantGroupResponse.VariantResponse>> addVariant(
            @PathVariable Long productId,
            @Valid @RequestBody CreateVariantRequest request) {
        ProductVariantGroupResponse.VariantResponse response = productVariantService.addVariant(productId, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.product.variant")),
                response
        ));
    }

    @PatchMapping("/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật variant")
    public ResponseEntity<SuccessResponse<ProductVariantGroupResponse.VariantResponse>> updateVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody UpdateVariantRequest request) {
        ProductVariantGroupResponse.VariantResponse response = productVariantService.updateVariant(productId, variantId, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.product.variant")),
                response
        ));
    }

    @DeleteMapping("/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa variant")
    public ResponseEntity<SuccessResponse<Void>> deleteVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        productVariantService.deleteVariant(productId, variantId);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.product.variant"))
        ));
    }
}
