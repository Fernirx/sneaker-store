package com.fernirx.sneakerapi.product.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.product.dto.request.CreateProductRequest;
import com.fernirx.sneakerapi.product.dto.request.InternalProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateProductRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductInternalResponse;
import com.fernirx.sneakerapi.product.service.ProductService;
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
@RequestMapping("/internal/products")
@RequiredArgsConstructor
@Tag(name = "Internal Product API", description = "Quản lý sản phẩm (nội bộ)")
public class InternalProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Danh sách sản phẩm")
    public ResponseEntity<PageResponse<ProductInternalResponse>> getAll(
            @ParameterObject @ModelAttribute InternalProductFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(productService.getInternalProducts(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Chi tiết sản phẩm")
    public ResponseEntity<SuccessResponse<ProductInternalResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(productService.getInternalById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thêm sản phẩm mới")
    public ResponseEntity<SuccessResponse<ProductInternalResponse>> create(
            @Valid @RequestBody CreateProductRequest request) {
        ProductInternalResponse response = productService.createProduct(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.product")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thông tin sản phẩm")
    public ResponseEntity<SuccessResponse<ProductInternalResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductInternalResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.product")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa sản phẩm")
    public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.product"))
        ));
    }
}
