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
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
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
    private final Slugify slugify;

    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> getBrands(BrandFilterRequest filter, Pageable pageable) {
        BrandFilterRequest publicFilter = new BrandFilterRequest(filter.search(), true);
        return brandRepository.findAll(BrandSpec.build(publicFilter), pageable)
                .map(brandMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug)
                .filter(Brand::getActive)
                .orElseThrow(() -> BusinessException.notFound("label.brand"));
        return brandMapper.toResponse(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BrandInternalResponse> getInternalBrands(BrandFilterRequest filter, Pageable pageable) {
        return brandRepository.findAll(BrandSpec.build(filter), pageable)
                .map(brandMapper::toInternalResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandInternalResponse getInternalById(Long id) {
        return brandMapper.toInternalResponse(findById(id));
    }

    @Override
    public BrandInternalResponse createBrand(CreateBrandRequest request) {
        if (brandRepository.existsByNameIgnoreCase(request.name())) {
            throw BusinessException.alreadyExists("label.brand");
        }
        String slug = generateUniqueSlug(request.name());
        Brand brand = new Brand();
        brand.setName(request.name());
        brand.setSlug(slug);
        brand.setDescription(request.description());
        brand.setLogoPublicId(request.logoPublicId());
        brand.setActive(true);
        return brandMapper.toInternalResponse(brandRepository.save(brand));
    }

    @Override
    public BrandInternalResponse updateBrand(Long id, UpdateBrandRequest request) {
        Brand brand = findById(id);
        if (request.name() != null && !request.name().equalsIgnoreCase(brand.getName())) {
            if (brandRepository.existsByNameIgnoreCase(request.name())) {
                throw BusinessException.alreadyExists("label.brand");
            }
        }
        brandMapper.updateBrand(request, brand);
        return brandMapper.toInternalResponse(brandRepository.save(brand));
    }

    @Override
    public BrandInternalResponse updateBrandSlug(Long id, String slug) {
        Brand brand = findById(id);
        if (!slug.equals(brand.getSlug()) && brandRepository.existsBySlug(slug)) {
            throw BusinessException.alreadyExists("label.slug");
        }
        brand.setSlug(slug);
        return brandMapper.toInternalResponse(brandRepository.save(brand));
    }

    @Override
    public void deleteBrand(Long id) {
        Brand brand = findById(id);
        if (!brand.getProducts().isEmpty()) {
            throw BusinessException.inUse("label.brand");
        }
        brandRepository.delete(brand);
    }

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

    private Brand findById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.brand"));
    }
}
