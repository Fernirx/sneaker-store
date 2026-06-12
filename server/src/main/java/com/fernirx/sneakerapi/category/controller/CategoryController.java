package com.fernirx.sneakerapi.category.controller;

import com.fernirx.sneakerapi.category.dto.request.CategoryFilterRequest;
import com.fernirx.sneakerapi.category.dto.response.CategoryResponse;
import com.fernirx.sneakerapi.category.service.CategoryService;
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
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Category API", description = "Danh mục sản phẩm")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Danh sách danh mục")
    public ResponseEntity<PageResponse<CategoryResponse>> getCategories(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long parentId,
            @PageableDefault(size = 50, sort = "displayOrder") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(
                categoryService.getCategories(new CategoryFilterRequest(search, null, parentId), pageable)
        ));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Chi tiết danh mục theo slug")
    public ResponseEntity<SuccessResponse<CategoryResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(SuccessResponse.of(categoryService.getBySlug(slug)));
    }
}
