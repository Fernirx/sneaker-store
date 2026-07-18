package com.fernirx.sneakerapi.banner.service;

import com.fernirx.sneakerapi.banner.dto.request.BannerFilterRequest;
import com.fernirx.sneakerapi.banner.dto.request.CreateBannerRequest;
import com.fernirx.sneakerapi.banner.dto.request.UpdateBannerRequest;
import com.fernirx.sneakerapi.banner.dto.response.BannerInternalResponse;
import com.fernirx.sneakerapi.banner.dto.response.BannerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BannerService {
    List<BannerResponse> getPublicBanners();

    Page<BannerInternalResponse> getInternalBanners(BannerFilterRequest filter, Pageable pageable);

    BannerInternalResponse getInternalById(Long id);

    BannerInternalResponse createBanner(CreateBannerRequest request);

    BannerInternalResponse updateBanner(Long id, UpdateBannerRequest request);

    void deleteBanner(Long id);
}
