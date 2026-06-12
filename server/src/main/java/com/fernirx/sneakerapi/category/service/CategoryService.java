package com.fernirx.sneakerapi.category.service;

import com.fernirx.sneakerapi.category.dto.request.CategoryFilterRequest;
import com.fernirx.sneakerapi.category.dto.request.CreateCategoryRequest;
import com.fernirx.sneakerapi.category.dto.request.UpdateCategoryRequest;
import com.fernirx.sneakerapi.category.dto.response.CategoryInternalResponse;
import com.fernirx.sneakerapi.category.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    Page<CategoryResponse> getCategories(CategoryFilterRequest filter, Pageable pageable);
    CategoryResponse getBySlug(String slug);
    Page<CategoryInternalResponse> getInternalCategories(CategoryFilterRequest filter, Pageable pageable);
    CategoryInternalResponse getInternalById(Long id);
    CategoryInternalResponse createCategory(CreateCategoryRequest request);
    CategoryInternalResponse updateCategory(Long id, UpdateCategoryRequest request);
    CategoryInternalResponse updateCategorySlug(Long id, String slug);
    void deleteCategory(Long id);
}
