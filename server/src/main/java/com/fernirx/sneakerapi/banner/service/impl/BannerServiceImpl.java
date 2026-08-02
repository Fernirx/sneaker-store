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

    /**
     * Lấy danh sách Banner đang hiển thị công khai (dành cho Front-end).
     * Luồng xử lý:
     * Truy vấn các Banner có trạng thái active = true và thời gian hiện tại 
     * nằm trong khoảng [startAt, endAt]. Sắp xếp theo displayOrder.
     */
    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> getPublicBanners() {
        return bannerRepository.findPublicActive(LocalDateTime.now())
                .stream()
                .map(bannerMapper::toResponse)
                .toList();
    }

    /**
     * Lấy danh sách Banner cho trang quản trị (CMS).
     * Luồng xử lý:
     * Trả về danh sách phân trang kèm theo bộ lọc động (JPA Specification).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BannerInternalResponse> getInternalBanners(BannerFilterRequest filter, Pageable pageable) {
        return bannerRepository.findAll(BannerSpec.build(filter), pageable)
                .map(bannerMapper::toInternalResponse);
    }

    /**
     * Lấy chi tiết một Banner theo ID cho CMS.
     */
    @Override
    @Transactional(readOnly = true)
    public BannerInternalResponse getInternalById(Long id) {
        return bannerMapper.toInternalResponse(findById(id));
    }

    /**
     * Tạo mới một Banner.
     * Luồng xử lý:
     * 1. Validate thời gian: Thời gian kết thúc (endAt) không được nhỏ hơn thời gian bắt đầu (startAt).
     * 2. Ánh xạ dữ liệu, nếu không truyền displayOrder thì mặc định là 0.
     * 3. Lưu vào DB với trạng thái kích hoạt (active = true).
     */
    @Override
    public BannerInternalResponse createBanner(CreateBannerRequest request) {
        validateDateRange(request.startAt(), request.endAt());
        
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

    /**
     * Cập nhật thông tin Banner.
     * Luồng xử lý:
     * 1. Tìm Banner theo ID, nếu không có ném ngoại lệ Not Found.
     * 2. Validate lại khoảng thời gian startAt - endAt nếu có thay đổi.
     * 3. Sử dụng MapStruct để đè các trường thay đổi từ DTO sang Entity.
     * 4. Lưu lại xuống DB.
     */
    @Override
    public BannerInternalResponse updateBanner(Long id, UpdateBannerRequest request) {
        Banner banner = findById(id);
        
        // Lấy thời gian mới nhất sau khi merge để validate
        LocalDateTime newStart = request.startAt() != null ? request.startAt() : banner.getStartAt();
        LocalDateTime newEnd = request.endAt() != null ? request.endAt() : banner.getEndAt();
        validateDateRange(newStart, newEnd);
        
        bannerMapper.updateBanner(request, banner);
        Banner saved = bannerRepository.save(banner);
        return bannerMapper.toInternalResponse(saved);
    }

    /**
     * Xóa cứng một Banner.
     * Luồng xử lý:
     * Tìm theo ID và xóa khỏi DB. Banners thường không ràng buộc khóa ngoại
     * nghiệp vụ quan trọng nên cho phép xóa cứng.
     */
    @Override
    public void deleteBanner(Long id) {
        bannerRepository.delete(findById(id));
    }

    /**
     * Hàm phụ trợ validate thời gian bắt đầu và kết thúc của Banner.
     */
    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw BusinessException.bad("label.date.invalid");
        }
    }

    /**
     * Hàm phụ trợ lấy thông tin Banner từ DB theo ID, ném lỗi 404 nếu không thấy.
     */
    private Banner findById(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.banner"));
    }
}
