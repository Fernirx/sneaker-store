package com.fernirx.sneakerapi.banner.controller;

import com.fernirx.sneakerapi.banner.dto.response.BannerResponse;
import com.fernirx.sneakerapi.banner.service.BannerService;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
@Tag(name = "Banner API", description = "Banner quảng bá trang chủ")
public class BannerController {
    private final BannerService bannerService;

    @GetMapping
    @Operation(summary = "Danh sách banner đang hiển thị")
    public ResponseEntity<SuccessResponse<List<BannerResponse>>> getBanners() {
        return ResponseEntity.ok(SuccessResponse.of(bannerService.getPublicBanners()));
    }
}
