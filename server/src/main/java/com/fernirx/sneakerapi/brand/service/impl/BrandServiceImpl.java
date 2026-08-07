package com.fernirx.sneakerapi.brand.service.impl;

import com.fernirx.sneakerapi.brand.dto.request.BrandFilterRequest;
import com.fernirx.sneakerapi.brand.dto.request.CreateBrandRequest;
import com.fernirx.sneakerapi.brand.dto.request.UpdateBrandRequest;
import com.fernirx.sneakerapi.brand.dto.response.BrandInternalResponse;
import com.fernirx.sneakerapi.brand.dto.response.BrandResponse;
import com.fernirx.sneakerapi.brand.entity.Brand;
import com.fernirx.sneakerapi.brand.mapper.BrandMapper;
import com.fernirx.sneakerapi.brand.repository.BrandRepository;
import com.fernirx.sneakerapi.brand.repository.BrandSpec;
import com.fernirx.sneakerapi.brand.service.BrandService;
import com.fernirx.sneakerapi.product.service.ProductService;
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
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final ProductService productService;
    private final Slugify slugify;
    private final PolicyFactory richTextHtmlPolicy;

    /**
     * Lấy danh sách Brand hiển thị công khai (dành cho Front-end).
     * Luồng xử lý:
     * Truy vấn danh sách Brand kèm bộ lọc, ép điều kiện luôn chỉ lấy các 
     * Brand đang kích hoạt (active = true).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> getBrands(BrandFilterRequest filter, Pageable pageable) {
        BrandFilterRequest publicFilter = new BrandFilterRequest(filter.search(), true);
        return brandRepository.findAll(BrandSpec.build(publicFilter), pageable)
                .map(brandMapper::toResponse);
    }

    /**
     * Lấy chi tiết Brand theo đường dẫn thân thiện (slug).
     * Luồng xử lý:
     * Tìm theo slug, lọc thêm điều kiện active = true. 
     * Trả về lỗi 404 nếu không tìm thấy.
     */
    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .filter(Brand::getActive)
                .orElseThrow(() -> BusinessException.notFound("label.brand"));
        return brandMapper.toResponse(brand);
    }

    /**
     * Lấy danh sách Brand dành cho trang quản trị (CMS).
     * Bỏ qua điều kiện active, hỗ trợ tìm kiếm linh hoạt.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BrandInternalResponse> getInternalBrands(BrandFilterRequest filter, Pageable pageable) {
        return brandRepository.findAll(BrandSpec.build(filter), pageable)
                .map(brandMapper::toInternalResponse);
    }

    /**
     * Lấy chi tiết Brand theo ID cho hệ thống quản trị.
     */
    @Override
    @Transactional(readOnly = true)
    public BrandInternalResponse getInternalById(Long id) {
        return brandMapper.toInternalResponse(findById(id));
    }

    /**
     * Tạo mới một Brand.
     * Luồng xử lý:
     * 1. Kiểm tra xem tên Brand có bị trùng lặp chưa (Bỏ qua viết hoa/thường).
     * 2. Sinh slug tự động từ tên, đảm bảo slug là duy nhất.
     * 3. Gán các thông vị cơ bản. Đặc biệt sanitize nội dung description bằng 
     *    Owasp Policy để phòng chống mã độc (XSS).
     * 4. Mặc định trạng thái kích hoạt (active = true).
     */
    @Override
    public BrandInternalResponse createBrand(CreateBrandRequest request) {
        if (brandRepository.existsByNameIgnoreCase(request.name())) {
            throw BusinessException.alreadyExists("label.brand");
        }
        
        String slug = generateUniqueSlug(request.name());
        Brand brand = new Brand();
        brand.setName(request.name());
        brand.setSlug(slug);
        
        String cleanDesc = request.description() != null 
                ? richTextHtmlPolicy.sanitize(request.description()) 
                : null;
        brand.setDescription(cleanDesc);
        
        brand.setLogoPublicId(request.logoPublicId());
        brand.setActive(true);
        
        return brandMapper.toInternalResponse(brandRepository.save(brand));
    }

    /**
     * Cập nhật thông tin cơ bản của Brand.
     * Luồng xử lý:
     * 1. Tìm Brand theo ID.
     * 2. Nếu tên thay đổi, kiểm tra chống trùng lặp tên với các Brand khác.
     * 3. Dùng MapStruct đè thông tin mới.
     * 4. Sanitize lại nội dung mô tả (nếu có) trước khi lưu để chặn mã độc.
     */
    @Override
    public BrandInternalResponse updateBrand(Long id, UpdateBrandRequest request) {
        Brand brand = findById(id);
        
        if (request.name() != null && !request.name().equalsIgnoreCase(brand.getName())) {
            if (brandRepository.existsByNameIgnoreCase(request.name())) {
                throw BusinessException.alreadyExists("label.brand");
            }
        }
        
        brandMapper.updateBrand(request, brand);
        if (request.description() != null) {
            brand.setDescription(richTextHtmlPolicy.sanitize(request.description()));
        }
        
        return brandMapper.toInternalResponse(brandRepository.save(brand));
    }

    /**
     * Cập nhật riêng đường dẫn thân thiện (slug) cho Brand (hỗ trợ SEO).
     * Luồng xử lý:
     * Nếu thay đổi slug, check chống trùng với các slug đã có trong hệ thống.
     */
    @Override
    public BrandInternalResponse updateBrandSlug(Long id, String slug) {
        Brand brand = findById(id);
        if (!slug.equals(brand.getSlug()) && brandRepository.existsBySlug(slug)) {
            throw BusinessException.alreadyExists("label.slug");
        }
        brand.setSlug(slug);
        return brandMapper.toInternalResponse(brandRepository.save(brand));
    }

    /**
     * Xóa một Brand.
     * Luồng xử lý:
     * 1. Tìm Brand cần xóa.
     * 2. Check xem Brand hiện tại có đang trống sản phẩm không. 
     *    Nếu vẫn còn sản phẩm -> Báo lỗi đang được sử dụng (IN_USE).
     * 3. Gọi DB xóa cứng Brand.
     */
    @Override
    public void delete(Long id) {
        Brand brand = findById(id);
        if (!brand.getProducts().isEmpty()) {
            throw BusinessException.inUse("label.brand");
        }
        brandRepository.delete(brand);
    }

    /**
     * Sinh tự động slug thân thiện (URL-safe) từ tên và đảm bảo duy nhất.
     */
    private String generateUniqueSlug(String name) {
        String base = slugify.slugify(name);
        if (!brandRepository.existsBySlug(base)) {
            return base;
        }
        int suffix = 1;
        String candidate;
        do {
            candidate = base + "-" + suffix++;
        } while (brandRepository.existsBySlug(candidate));
        return candidate;
    }

    /**
     * Tìm kiếm Brand theo ID, ném lỗi 404 nếu không tìm thấy.
     */
    private Brand findById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.brand"));
    }
}
