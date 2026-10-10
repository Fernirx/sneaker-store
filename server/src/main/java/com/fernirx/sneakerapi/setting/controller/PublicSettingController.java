package com.fernirx.sneakerapi.setting.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.setting.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/public/settings")
@RequiredArgsConstructor
@Tag(name = "Public Store Setting API", description = "Chính sách giá công khai cho khách hàng (free-ship...)")
public class PublicSettingController {
    private final SettingService settingService;

    @GetMapping("/free-ship-threshold")
    @Operation(summary = "Ngưỡng miễn phí vận chuyển")
    public ResponseEntity<SuccessResponse<BigDecimal>> getFreeShipThreshold() {
        return ResponseEntity.ok(SuccessResponse.of(settingService.getStoreSetting().freeShipThreshold()));
    }
}
