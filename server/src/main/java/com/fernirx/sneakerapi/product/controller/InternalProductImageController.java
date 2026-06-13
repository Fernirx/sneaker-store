package com.fernirx.sneakerapi.product.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.product.dto.request.AddImageRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateImageRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductImageGroupResponse;
import com.fernirx.sneakerapi.product.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/products/{productId}/images")
@RequiredArgsConstructor
@Tag(name = "Internal Product Image API", description = "Quản lý ảnh sản phẩm (nội bộ)")
public class InternalProductImageController {

    private final ProductImageService productImageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Danh sách ảnh theo màu")
    public ResponseEntity<SuccessResponse<List<ProductImageGroupResponse>>> getImages(
            @PathVariable Long productId) {
        return ResponseEntity.ok(SuccessResponse.of(productImageService.getImages(productId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm ảnh mới")
    public ResponseEntity<SuccessResponse<ProductImageGroupResponse.ImageResponse>> addImage(
            @PathVariable Long productId,
            @Valid @RequestBody AddImageRequest request) {
        ProductImageGroupResponse.ImageResponse response = productImageService.addImage(productId, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.product.image")),
                response
        ));
    }

    @PatchMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật ảnh (primary, thứ tự)")
    public ResponseEntity<SuccessResponse<ProductImageGroupResponse.ImageResponse>> updateImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @RequestBody UpdateImageRequest request) {
        ProductImageGroupResponse.ImageResponse response = productImageService.updateImage(productId, imageId, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.product.image")),
                response
        ));
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa ảnh")
    public ResponseEntity<SuccessResponse<Void>> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        productImageService.deleteImage(productId, imageId);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.product.image"))
        ));
    }
}
