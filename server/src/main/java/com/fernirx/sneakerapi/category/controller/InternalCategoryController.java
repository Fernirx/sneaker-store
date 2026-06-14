package com.fernirx.sneakerapi.category.controller;

import com.fernirx.sneakerapi.category.dto.request.CategoryFilterRequest;
import com.fernirx.sneakerapi.category.dto.request.CreateCategoryRequest;
import com.fernirx.sneakerapi.category.dto.request.SlugUpdateRequest;
import com.fernirx.sneakerapi.category.dto.request.UpdateCategoryRequest;
import com.fernirx.sneakerapi.category.dto.response.CategoryInternalResponse;
import com.fernirx.sneakerapi.category.service.CategoryService;
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
@RequestMapping("/internal/categories")
@RequiredArgsConstructor
@Tag(name = "Internal Category API", description = "Quản lý danh mục (nội bộ)")
public class InternalCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'MARKETING')")
    @Operation(summary = "Danh sách danh mục")
    public ResponseEntity<PageResponse<CategoryInternalResponse>> getInternalCategories(
            @ParameterObject @ModelAttribute CategoryFilterRequest filter,
            @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(categoryService.getInternalCategories(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'MARKETING')")
    @Operation(summary = "Chi tiết danh mục")
    public ResponseEntity<SuccessResponse<CategoryInternalResponse>> getInternalById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(categoryService.getInternalById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo danh mục mới")
    public ResponseEntity<SuccessResponse<CategoryInternalResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryInternalResponse response = categoryService.createCategory(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.category")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Cập nhật danh mục")
    public ResponseEntity<SuccessResponse<CategoryInternalResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryInternalResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.category")),
                response
        ));
    }

    @PatchMapping("/{id}/slug")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật slug danh mục")
    public ResponseEntity<SuccessResponse<CategoryInternalResponse>> updateCategorySlug(
            @PathVariable Long id,
            @Valid @RequestBody SlugUpdateRequest request) {
        CategoryInternalResponse response = categoryService.updateCategorySlug(id, request.slug());
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.category")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa danh mục")
    public ResponseEntity<SuccessResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.category"))
        ));
    }
}
