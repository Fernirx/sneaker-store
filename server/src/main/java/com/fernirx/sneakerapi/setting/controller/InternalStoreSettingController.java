package com.fernirx.sneakerapi.setting.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.setting.dto.request.CreateStoreSettingRequest;
import com.fernirx.sneakerapi.setting.dto.request.UpdateStoreSettingRequest;
import com.fernirx.sneakerapi.setting.dto.response.StoreSettingResponse;
import com.fernirx.sneakerapi.setting.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/settings/store")
@RequiredArgsConstructor
@Tag(name = "Internal Store Setting API", description = "Chính sách giá/ưu đãi - điểm tích lũy, free-ship, hạng thành viên (nội bộ)")
public class InternalStoreSettingController {
    private final SettingService settingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Xem chính sách giá/ưu đãi")
    public ResponseEntity<SuccessResponse<StoreSettingResponse>> get() {
        return ResponseEntity.ok(SuccessResponse.of(settingService.getStoreSetting()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Khởi tạo chính sách giá/ưu đãi (chỉ 1 lần)")
    public ResponseEntity<SuccessResponse<StoreSettingResponse>> create(
            @Valid @RequestBody CreateStoreSettingRequest request) {
        StoreSettingResponse response = settingService.createStoreSetting(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.storeSetting")),
                response
        ));
    }

    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Cập nhật chính sách giá/ưu đãi")
    public ResponseEntity<SuccessResponse<StoreSettingResponse>> update(
            @Valid @RequestBody UpdateStoreSettingRequest request) {
        StoreSettingResponse response = settingService.updateStoreSetting(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.storeSetting")),
                response
        ));
    }
}
