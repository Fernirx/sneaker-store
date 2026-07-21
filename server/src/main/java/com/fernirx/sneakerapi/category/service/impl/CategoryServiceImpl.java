package com.fernirx.sneakerapi.category.service.impl;

import com.fernirx.sneakerapi.category.dto.request.CategoryFilterRequest;
import com.fernirx.sneakerapi.category.dto.request.CreateCategoryRequest;
import com.fernirx.sneakerapi.category.dto.request.UpdateCategoryRequest;
import com.fernirx.sneakerapi.category.dto.response.CategoryInternalResponse;
import com.fernirx.sneakerapi.category.dto.response.CategoryResponse;
import com.fernirx.sneakerapi.category.entity.Category;
import com.fernirx.sneakerapi.category.mapper.CategoryMapper;
import com.fernirx.sneakerapi.category.repository.CategoryRepository;
import com.fernirx.sneakerapi.category.repository.CategorySpec;
import com.fernirx.sneakerapi.category.service.CategoryService;
import com.fernirx.sneakerapi.product.service.ProductCategoryService;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.owasp.html.PolicyFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductCategoryService productCategoryService;
    private final Slugify slugify;
    private final PolicyFactory richTextHtmlPolicy;

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getCategories(CategoryFilterRequest filter, Pageable pageable) {
        CategoryFilterRequest publicFilter = new CategoryFilterRequest(filter.search(), true, filter.parentId());
        return categoryRepository.findAll(CategorySpec.build(publicFilter), pageable)
                .map(categoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .filter(Category::getActive)
                .orElseThrow(() -> BusinessException.notFound("label.category"));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryInternalResponse> getInternalCategories(CategoryFilterRequest filter, Pageable pageable) {
        return categoryRepository.findAll(CategorySpec.build(filter), pageable)
                .map(categoryMapper::toInternalResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryInternalResponse getInternalById(Long id) {
        return categoryMapper.toInternalResponse(findById(id));
    }

    @Override
    public CategoryInternalResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw BusinessException.alreadyExists("label.category");
        }
        String slug = generateUniqueSlug(request.name());

        Category category = new Category();
        category.setName(request.name());
        category.setSlug(slug);
        category.setDescription(request.description() != null ? richTextHtmlPolicy.sanitize(request.description()) : null);
        category.setImagePublicId(request.imagePublicId());
        category.setDisplayOrder(request.displayOrder());
        category.setActive(true);
        if (request.parentId() != null) {
            category.setParent(findById(request.parentId()));
        }
        Category saved = categoryRepository.save(category);
        return categoryMapper.toInternalResponse(categoryRepository.findById(saved.getId()).orElseThrow());
    }

    @Override
    public CategoryInternalResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = findById(id);
        if (request.name() != null && !request.name().equalsIgnoreCase(category.getName())) {
            if (categoryRepository.existsByNameIgnoreCase(request.name())) {
                throw BusinessException.alreadyExists("label.category");
            }
        }
        categoryMapper.updateCategory(request, category);
        if (request.description() != null) {
            category.setDescription(richTextHtmlPolicy.sanitize(request.description()));
        }
        categoryRepository.save(category);
        return categoryMapper.toInternalResponse(categoryRepository.findById(id).orElseThrow());
    }

    @Override
    public CategoryInternalResponse updateCategorySlug(Long id, String slug) {
        Category category = findById(id);
        if (!slug.equals(category.getSlug()) && categoryRepository.existsBySlug(slug)) {
            throw BusinessException.alreadyExists("label.slug");
        }
        category.setSlug(slug);
        return categoryMapper.toInternalResponse(categoryRepository.save(category));
    }

    @Override
    public void reassignAndDelete(Long id, Long reassignToId) {
        Category category = findById(id);
        if (reassignToId != null) {
            findById(reassignToId); // validate đích
            productCategoryService.reassignCategory(id, reassignToId);
        } else if (!category.getProductCategories().isEmpty()) {
            throw BusinessException.inUse("label.category");
        }
        categoryRepository.delete(category);
    }

    private String generateUniqueSlug(String name) {
        String base = slugify.slugify(name);
        if (!categoryRepository.existsBySlug(base)) {
            return base;
        }
        int suffix = 1;
        String candidate;
        do {
            candidate = base + "-" + suffix++;
        } while (categoryRepository.existsBySlug(candidate));
        return candidate;
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.category"));
    }
}
