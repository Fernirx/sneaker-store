package com.fernirx.sneakerapi.banner.controller;

import com.fernirx.sneakerapi.banner.dto.request.BannerFilterRequest;
import com.fernirx.sneakerapi.banner.dto.request.CreateBannerRequest;
import com.fernirx.sneakerapi.banner.dto.request.UpdateBannerRequest;
import com.fernirx.sneakerapi.banner.dto.response.BannerInternalResponse;
import com.fernirx.sneakerapi.banner.service.BannerService;
import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/banners")
@RequiredArgsConstructor
@Tag(name = "Internal Banner API", description = "Quản lý banner (nội bộ)")
public class InternalBannerController {
    private final BannerService bannerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'MARKETING')")
    @Operation(summary = "Danh sách banner")
    public ResponseEntity<PageResponse<BannerInternalResponse>> getInternalBanners(
            @ParameterObject @ModelAttribute BannerFilterRequest filter,
            @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(bannerService.getInternalBanners(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'MARKETING')")
    @Operation(summary = "Chi tiết banner")
    public ResponseEntity<SuccessResponse<BannerInternalResponse>> getInternalById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(bannerService.getInternalById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo banner mới")
    public ResponseEntity<SuccessResponse<BannerInternalResponse>> createBanner(
            @Valid @RequestBody CreateBannerRequest request) {
        BannerInternalResponse response = bannerService.createBanner(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.banner")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Cập nhật banner")
    public ResponseEntity<SuccessResponse<BannerInternalResponse>> updateBanner(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBannerRequest request) {
        BannerInternalResponse response = bannerService.updateBanner(id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.banner")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa banner")
    public ResponseEntity<SuccessResponse<Void>> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.banner"))
        ));
    }
}
