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

    /**
     * Lấy danh sách danh mục (công khai) theo bộ lọc.
     * Tự động lọc chỉ lấy các danh mục đang hoạt động (active = true).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getCategories(CategoryFilterRequest filter, Pageable pageable) {
        CategoryFilterRequest publicFilter = new CategoryFilterRequest(filter.search(), true, filter.parentId());
        return categoryRepository.findAll(CategorySpec.build(publicFilter), pageable)
                .map(categoryMapper::toResponse);
    }

    /**
     * Lấy chi tiết danh mục bằng slug (cho Frontend).
     * Chỉ trả về khi danh mục có trạng thái hoạt động (active = true).
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .filter(Category::getActive)
                .orElseThrow(() -> BusinessException.notFound("label.category"));
        return categoryMapper.toResponse(category);
    }

    /**
     * Lấy danh sách danh mục (nội bộ/CMS) theo bộ lọc.
     * Trả về toàn bộ kể cả danh mục bị ẩn.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CategoryInternalResponse> getInternalCategories(CategoryFilterRequest filter, Pageable pageable) {
        return categoryRepository.findAll(CategorySpec.build(filter), pageable)
                .map(categoryMapper::toInternalResponse);
    }

    /**
     * Lấy chi tiết danh mục theo ID (nội bộ/CMS).
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryInternalResponse getInternalById(Long id) {
        return categoryMapper.toInternalResponse(findById(id));
    }

    /**
     * Tạo danh mục mới.
     * Luồng xử lý:
     * 1. Kiểm tra trùng lặp tên (không phân biệt hoa/thường).
     * 2. Sinh slug từ tên (nếu trùng slug sẽ tự động thêm hậu tố -1, -2).
     * 3. Sanitize (làm sạch) nội dung mô tả để tránh XSS.
     * 4. Gắn parent nếu có truyền parentId.
     */
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

    /**
     * Cập nhật thông tin danh mục.
     * Luồng xử lý:
     * 1. Kiểm tra không đổi tên trùng với danh mục khác.
     * 2. Sanitize nội dung mô tả.
     * 3. Xử lý parent: Nếu clearParent = true -> gỡ bỏ parent. 
     *    Ngược lại nếu đổi parent mới -> kiểm tra chống vòng lặp đệ quy.
     */
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
        
        if (Boolean.TRUE.equals(request.clearParent())) {
            category.setParent(null);
        } else if (request.parentId() != null) {
            Category parent = findById(request.parentId());
            validateNoCyclicReference(category, parent);
            category.setParent(parent);
        }
        
        categoryRepository.save(category);
        return categoryMapper.toInternalResponse(categoryRepository.findById(id).orElseThrow());
    }

    /**
     * Cập nhật slug SEO riêng rẽ cho danh mục.
     */
    @Override
    public CategoryInternalResponse updateCategorySlug(Long id, String slug) {
        Category category = findById(id);
        if (!slug.equals(category.getSlug()) && categoryRepository.existsBySlug(slug)) {
            throw BusinessException.alreadyExists("label.slug");
        }
        category.setSlug(slug);
        return categoryMapper.toInternalResponse(categoryRepository.save(category));
    }

    /**
     * Xóa danh mục.
     * Luồng xử lý:
     * 1. Kiểm tra rỗng (không chứa sản phẩm, không chứa danh mục con).
     *    Nếu không -> ném lỗi IN_USE
     * 2. Thực thi xóa cứng danh mục.
     */
    @Override
    public void delete(Long id) {
        Category category = findById(id);
        if (!category.getProductCategories().isEmpty() || !category.getCategories().isEmpty()) {
            throw BusinessException.inUse("label.category");
        }
        categoryRepository.delete(category);
    }

    // ---- Private helpers ----

    /**
     * Tự động sinh ra chuỗi slug URL-safe từ tên danh mục. 
     * Nếu trùng thì nối thêm -1, -2...
     */
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

    /**
     * Lấy danh mục theo ID, văng lỗi 404 nếu không tồn tại.
     */
    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.category"));
    }

    /**
     * Kiểm tra chống vòng lặp (Cyclic Reference) khi đổi parent cho category.
     * Bằng cách truy ngược từ newParent lên root, nếu đụng phải category -> Lỗi.
     */
    private void validateNoCyclicReference(Category category, Category newParent) {
        Category current = newParent;
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                throw BusinessException.bad("label.category");
            }
            current = current.getParent();
        }
    }
}
