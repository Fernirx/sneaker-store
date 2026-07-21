package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.product.dto.request.AssignCategoriesRequest;
import com.fernirx.sneakerapi.product.dto.response.CategoryBriefResponse;

import java.util.List;

public interface ProductCategoryService {

    List<CategoryBriefResponse> getCategories(Long productId);

    List<CategoryBriefResponse> assignCategories(Long productId, AssignCategoriesRequest request);

    // Cross-module (Category) - chuyển toàn bộ sản phẩm đang gán fromCategoryId sang toCategoryId,
    // dùng khi xóa danh mục theo lựa chọn "chuyển sang danh mục khác" thay vì gỡ hẳn.
    void reassignCategory(Long fromCategoryId, Long toCategoryId);
}
