package com.fernirx.sneakerapi.shipping.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;
import com.fernirx.sneakerapi.shipping.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/shipping")
@RequiredArgsConstructor
@Tag(name = "Public Shipping API", description = "API giao nhận & thông tin địa chỉ công khai")
public class PublicShippingController {
    private final ShippingService shippingService;

    @GetMapping("/provinces")
    @Operation(summary = "Danh sách Tỉnh/Thành phố")
    public ResponseEntity<SuccessResponse<List<LocalityResponse>>> getProvinces() {
        return ResponseEntity.ok(SuccessResponse.of(shippingService.getProvinces()));
    }

    @GetMapping("/wards")
    @Operation(summary = "Danh sách Phường/Xã theo Tỉnh/Thành phố")
    public ResponseEntity<SuccessResponse<List<LocalityResponse>>> getWards(@RequestParam Integer provinceId) {
        return ResponseEntity.ok(SuccessResponse.of(shippingService.getWardsByProvince(provinceId)));
    }
}
