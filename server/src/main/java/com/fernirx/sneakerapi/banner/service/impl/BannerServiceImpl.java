package com.fernirx.sneakerapi.banner.service.impl;

import com.fernirx.sneakerapi.banner.dto.request.BannerFilterRequest;
import com.fernirx.sneakerapi.banner.dto.request.CreateBannerRequest;
import com.fernirx.sneakerapi.banner.dto.request.UpdateBannerRequest;
import com.fernirx.sneakerapi.banner.dto.response.BannerInternalResponse;
import com.fernirx.sneakerapi.banner.dto.response.BannerResponse;
import com.fernirx.sneakerapi.banner.entity.Banner;
import com.fernirx.sneakerapi.banner.mapper.BannerMapper;
import com.fernirx.sneakerapi.banner.repository.BannerRepository;
import com.fernirx.sneakerapi.banner.repository.BannerSpec;
import com.fernirx.sneakerapi.banner.service.BannerService;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {
    private final BannerRepository bannerRepository;
    private final BannerMapper bannerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> getPublicBanners() {
        return bannerRepository.findPublicActive(LocalDateTime.now())
                .stream()
                .map(bannerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BannerInternalResponse> getInternalBanners(BannerFilterRequest filter, Pageable pageable) {
        return bannerRepository.findAll(BannerSpec.build(filter), pageable)
                .map(bannerMapper::toInternalResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BannerInternalResponse getInternalById(Long id) {
        return bannerMapper.toInternalResponse(findById(id));
    }

    @Override
    public BannerInternalResponse createBanner(CreateBannerRequest request) {
        Banner banner = new Banner();
        banner.setTitle(request.title());
        banner.setImagePublicId(request.imagePublicId());
        banner.setLinkUrl(request.linkUrl());
        banner.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
        banner.setStartAt(request.startAt());
        banner.setEndAt(request.endAt());
        banner.setActive(true);
        Banner saved = bannerRepository.save(banner);
        return bannerMapper.toInternalResponse(saved);
    }

    @Override
    public BannerInternalResponse updateBanner(Long id, UpdateBannerRequest request) {
        Banner banner = findById(id);
        bannerMapper.updateBanner(request, banner);
        Banner saved = bannerRepository.save(banner);
        return bannerMapper.toInternalResponse(saved);
    }

    @Override
    public void deleteBanner(Long id) {
        bannerRepository.delete(findById(id));
    }

    private Banner findById(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.banner"));
    }
}
