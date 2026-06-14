package com.fernirx.sneakerapi.brand.service;

import com.fernirx.sneakerapi.brand.dto.request.BrandFilterRequest;
import com.fernirx.sneakerapi.brand.dto.request.CreateBrandRequest;
import com.fernirx.sneakerapi.brand.dto.request.UpdateBrandRequest;
import com.fernirx.sneakerapi.brand.dto.response.BrandInternalResponse;
import com.fernirx.sneakerapi.brand.dto.response.BrandResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BrandService {
    Page<BrandResponse> getBrands(BrandFilterRequest filter, Pageable pageable);
    BrandResponse getBySlug(String slug);
    Page<BrandInternalResponse> getInternalBrands(BrandFilterRequest filter, Pageable pageable);
    BrandInternalResponse getInternalById(Long id);
    BrandInternalResponse createBrand(CreateBrandRequest request);
    BrandInternalResponse updateBrand(Long id, UpdateBrandRequest request);
    BrandInternalResponse updateBrandSlug(Long id, String slug);
    void deleteBrand(Long id);
}
