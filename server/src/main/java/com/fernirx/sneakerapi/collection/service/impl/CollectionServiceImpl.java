package com.fernirx.sneakerapi.collection.service.impl;

import com.fernirx.sneakerapi.collection.dto.request.CollectionFilterRequest;
import com.fernirx.sneakerapi.collection.dto.request.CreateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.request.UpdateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.response.CollectionInternalResponse;
import com.fernirx.sneakerapi.collection.dto.response.CollectionResponse;
import com.fernirx.sneakerapi.collection.entity.Collection;
import com.fernirx.sneakerapi.collection.mapper.CollectionMapper;
import com.fernirx.sneakerapi.collection.repository.CollectionRepository;
import com.fernirx.sneakerapi.collection.repository.CollectionSpec;
import com.fernirx.sneakerapi.collection.service.CollectionService;
import com.fernirx.sneakerapi.product.service.ProductCollectionService;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.owasp.html.PolicyFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;
    private final CollectionMapper collectionMapper;
    private final ProductCollectionService productCollectionService;
    private final Slugify slugify;
    private final PolicyFactory richTextHtmlPolicy;

    /**
     * Lấy danh sách Collection hiển thị công khai (dành cho Front-end).
     * Tự động ép điều kiện luôn chỉ lấy các Collection đang kích hoạt (active = true).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CollectionResponse> getCollections(CollectionFilterRequest filter, Pageable pageable) {
        CollectionFilterRequest publicFilter = new CollectionFilterRequest(filter.search(), true);
        return collectionRepository.findAll(CollectionSpec.build(publicFilter), pageable)
                .map(collectionMapper::toResponse);
    }

    /**
     * Lấy chi tiết Collection theo đường dẫn thân thiện (slug).
     * Chỉ trả về khi Collection có trạng thái hoạt động (active = true).
     */
    @Override
    @Transactional(readOnly = true)
    public CollectionResponse getBySlug(String slug) {
        Collection collection = collectionRepository.findBySlug(slug)
                .filter(Collection::getActive)
                .orElseThrow(() -> BusinessException.notFound("label.collection"));
        return collectionMapper.toResponse(collection);
    }

    /**
     * Lấy danh sách Collection dành cho trang quản trị (CMS).
     * Bỏ qua điều kiện active, hỗ trợ tìm kiếm linh hoạt.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CollectionInternalResponse> getInternalCollections(CollectionFilterRequest filter, Pageable pageable) {
        return collectionRepository.findAll(CollectionSpec.build(filter), pageable)
                .map(collectionMapper::toInternalResponse);
    }

    /**
     * Lấy chi tiết Collection theo ID (hệ thống quản trị).
     */
    @Override
    @Transactional(readOnly = true)
    public CollectionInternalResponse getInternalById(Long id) {
        return collectionMapper.toInternalResponse(findById(id));
    }

    /**
     * Tạo mới một Collection.
     * Luồng xử lý:
     * 1. Kiểm tra tên không trùng lặp.
     * 2. Validate thời gian: Ngày kết thúc không được nhỏ hơn ngày bắt đầu.
     * 3. Tự động sinh slug duy nhất từ tên.
     * 4. Sanitize mô tả (chống XSS) và lưu xuống DB.
     */
    @Override
    public CollectionInternalResponse createCollection(CreateCollectionRequest request) {
        if (collectionRepository.existsByNameIgnoreCase(request.name())) {
            throw BusinessException.alreadyExists("label.collection");
        }
        
        validateDates(request.launchDate(), request.endDate());
        
        String slug = generateUniqueSlug(request.name());
        Collection collection = new Collection();
        collection.setName(request.name());
        collection.setSlug(slug);

        String cleanDesc = request.description() != null
                ? richTextHtmlPolicy.sanitize(request.description())
                : null;
        collection.setDescription(cleanDesc);

        collection.setImagePublicId(request.imagePublicId());
        collection.setLaunchDate(request.launchDate());
        collection.setEndDate(request.endDate());
        collection.setActive(true);

        Collection saved = collectionRepository.save(collection);
        return collectionMapper.toInternalResponse(
            collectionRepository.findById(saved.getId()).orElseThrow()
        );
    }

    /**
     * Cập nhật thông tin Collection.
     * Luồng xử lý:
     * 1. Kiểm tra không đổi tên trùng với Collection khác.
     * 2. Validate thời gian: Ngày kết thúc không được nhỏ hơn ngày bắt đầu.
     * 3. Dùng MapStruct đè thông tin mới.
     * 4. Sanitize lại nội dung mô tả (nếu có).
     */
    @Override
    public CollectionInternalResponse updateCollection(Long id, UpdateCollectionRequest request) {
        Collection collection = findById(id);
        
        if (request.name() != null && !request.name().equalsIgnoreCase(collection.getName())) {
            if (collectionRepository.existsByNameIgnoreCase(request.name())) {
                throw BusinessException.alreadyExists("label.collection");
            }
        }
        
        // Kiểm tra logic thời gian với data mới (nếu request truyền null thì lấy field cũ)
        LocalDate launchDate = request.launchDate() != null ? request.launchDate() : collection.getLaunchDate();
        LocalDate endDate = request.endDate() != null ? request.endDate() : collection.getEndDate();
        validateDates(launchDate, endDate);
        
        collectionMapper.updateCollection(request, collection);
        String cleanDesc = request.description() != null
                ? richTextHtmlPolicy.sanitize(request.description())
                : null;
        collection.setDescription(cleanDesc);
        
        collectionRepository.save(collection);
        return collectionMapper.toInternalResponse(
            collectionRepository.findById(id).orElseThrow()
        );
    }

    /**
     * Cập nhật riêng đường dẫn thân thiện (slug) cho Collection.
     */
    @Override
    public CollectionInternalResponse updateCollectionSlug(Long id, String slug) {
        Collection collection = findById(id);
        if (!slug.equals(collection.getSlug()) && collectionRepository.existsBySlug(slug)) {
            throw BusinessException.alreadyExists("label.slug");
        }
        collection.setSlug(slug);
        return collectionMapper.toInternalResponse(collectionRepository.save(collection));
    }

    /**
     * Xóa Collection. Hỗ trợ chuyển giao sản phẩm sang Collection khác trước khi xóa.
     * Luồng xử lý:
     * 1. Nếu có chỉ định reassignToId: 
     *    - Kiểm tra chống chuyển gán cho chính nó (Lỗi tự gán).
     *    - Gọi service chuyển toàn bộ Sản phẩm sang Collection mới.
     * 2. Nếu không chỉ định reassignToId:
     *    - Bắt buộc Collection phải trống (không chứa sản phẩm).
     * 3. Thực thi xóa cứng.
     */
    @Override
    public void reassignAndDelete(Long id, Long reassignToId) {
        Collection collection = findById(id);
        
        if (reassignToId != null) {
            if (id.equals(reassignToId)) {
                throw BusinessException.bad("label.collection");
            }
            findById(reassignToId);
            productCollectionService.reassignCollection(id, reassignToId);
        } else if (!collection.getProductCollections().isEmpty()) {
            throw BusinessException.inUse("label.collection");
        }
        
        collectionRepository.delete(collection);
    }

    // ---- Private helpers ----

    /**
     * Kiểm tra tính hợp lệ của mốc thời gian: 
     * Ngày kết thúc không được phép diễn ra trước ngày ra mắt.
     */
    private void validateDates(LocalDate launchDate, LocalDate endDate) {
        if (launchDate != null && endDate != null && endDate.isBefore(launchDate)) {
            throw BusinessException.bad("label.collection");
        }
    }

    /**
     * Tự động sinh ra chuỗi slug URL-safe từ tên. 
     * Đảm bảo không trùng lặp bằng cách thêm hậu tố số.
     */
    private String generateUniqueSlug(String name) {
        String base = slugify.slugify(name);
        if (!collectionRepository.existsBySlug(base)) {
            return base;
        }
        int suffix = 1;
        String candidate;
        do {
            candidate = base + "-" + suffix++;
        } while (collectionRepository.existsBySlug(candidate));
        return candidate;
    }

    /**
     * Lấy Collection theo ID, văng lỗi 404 nếu không tìm thấy.
     */
    private Collection findById(Long id) {
        return collectionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.collection"));
    }
}
