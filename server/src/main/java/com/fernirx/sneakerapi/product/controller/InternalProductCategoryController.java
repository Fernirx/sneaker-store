package com.fernirx.sneakerapi.product.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.product.dto.request.AssignCategoriesRequest;
import com.fernirx.sneakerapi.product.dto.response.CategoryBriefResponse;
import com.fernirx.sneakerapi.product.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/products/{productId}/categories")
@RequiredArgsConstructor
@Tag(name = "Internal Product Category API", description = "Quản lý danh mục sản phẩm (nội bộ)")
public class InternalProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Danh sách danh mục của sản phẩm")
    public ResponseEntity<SuccessResponse<List<CategoryBriefResponse>>> getCategories(
            @PathVariable Long productId) {
        return ResponseEntity.ok(SuccessResponse.of(productCategoryService.getCategories(productId)));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gán lại danh mục cho sản phẩm (thay thế toàn bộ)")
    public ResponseEntity<SuccessResponse<List<CategoryBriefResponse>>> assignCategories(
            @PathVariable Long productId,
            @Valid @RequestBody AssignCategoriesRequest request) {
        List<CategoryBriefResponse> response = productCategoryService.assignCategories(productId, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.category")),
                response
        ));
    }
}
