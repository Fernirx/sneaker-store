package com.fernirx.sneakerapi.product.service.impl;

import com.fernirx.sneakerapi.category.entity.Category;
import com.fernirx.sneakerapi.category.repository.CategoryRepository;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.product.dto.request.AssignCategoriesRequest;
import com.fernirx.sneakerapi.product.dto.response.CategoryBriefResponse;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductCategory;
import com.fernirx.sneakerapi.product.repository.ProductCategoryRepository;
import com.fernirx.sneakerapi.product.repository.ProductRepository;
import com.fernirx.sneakerapi.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Lấy danh sách các danh mục (Category) mà sản phẩm này đang thuộc về.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryBriefResponse> getCategories(Long productId) {
        findProduct(productId);
        return productCategoryRepository.findByProductIdOrderByCategoryDisplayOrderAsc(productId)
                .stream()
                .map(pc -> new CategoryBriefResponse(
                        pc.getCategory().getId(),
                        pc.getCategory().getName(),
                        pc.getCategory().getSlug()
                ))
                .toList();
    }

    /**
     * Gán danh sách danh mục cho sản phẩm.
     * Xóa toàn bộ liên kết cũ và tạo liên kết mới để đảm bảo tính toàn vẹn (Full Replace).
     */
    @Override
    public List<CategoryBriefResponse> assignCategories(Long productId, AssignCategoriesRequest request) {
        Product product = findProduct(productId);

        List<Category> categories = categoryRepository.findAllById(request.categoryIds());
        if (categories.size() != request.categoryIds().size()) {
            throw BusinessException.notFound("label.category");
        }

        productCategoryRepository.deleteByProductId(productId);

        List<ProductCategory> newLinks = categories.stream()
                .map(category -> {
                    ProductCategory pc = new ProductCategory();
                    pc.setProduct(product);
                    pc.setCategory(category);
                    return pc;
                })
                .toList();
        productCategoryRepository.saveAll(newLinks);

        return categories.stream()
                .map(c -> new CategoryBriefResponse(c.getId(), c.getName(), c.getSlug()))
                .toList();
    }

    /**
     * Chuyển tất cả sản phẩm từ danh mục cũ sang danh mục mới (dùng khi gộp hoặc xóa danh mục).
     * Xóa các liên kết trùng lặp trước khi thực hiện bulk update để tránh vi phạm Unique Constraint.
     */
    @Override
    public void reassignCategory(Long fromCategoryId, Long toCategoryId) {
        productCategoryRepository.deleteDuplicatesForReassign(fromCategoryId, toCategoryId);
        productCategoryRepository.bulkReassignCategory(fromCategoryId, categoryRepository.getReferenceById(toCategoryId));
    }

    /**
     * Helper tìm sản phẩm theo ID.
     */
    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.notFound("label.product"));
    }
}
