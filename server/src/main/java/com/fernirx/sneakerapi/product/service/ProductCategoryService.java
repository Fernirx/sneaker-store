package com.fernirx.sneakerapi.product.service;

import com.fernirx.sneakerapi.product.dto.request.AssignCategoriesRequest;
import com.fernirx.sneakerapi.product.dto.response.CategoryBriefResponse;

import java.util.List;

public interface ProductCategoryService {

    List<CategoryBriefResponse> getCategories(Long productId);

    List<CategoryBriefResponse> assignCategories(Long productId, AssignCategoriesRequest request);
}
