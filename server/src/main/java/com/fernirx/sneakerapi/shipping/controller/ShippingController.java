package com.fernirx.sneakerapi.shipping.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.shipping.dto.request.CalculateShippingFeeRequest;
import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;
import com.fernirx.sneakerapi.shipping.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/public/shipping")
@RequiredArgsConstructor
@Tag(name = "Public Shipping API", description = "API giao nhận & thông tin địa chỉ công khai")
public class ShippingController {
    private final ShippingService shippingService;

    @GetMapping("/provinces")
    @Operation(summary = "Danh sách Tỉnh/Thành phố")
    public ResponseEntity<SuccessResponse<List<LocalityResponse>>> getProvinces() {
        return ResponseEntity.ok(SuccessResponse.of(shippingService.getProvinces()));
    }

    @GetMapping("/districts")
    @Operation(summary = "Danh sách Quận/Huyện theo Tỉnh/Thành phố")
    public ResponseEntity<SuccessResponse<List<LocalityResponse>>> getDistricts(@RequestParam Integer provinceId) {
        return ResponseEntity.ok(SuccessResponse.of(shippingService.getDistricts(provinceId)));
    }

    @GetMapping("/wards")
    @Operation(summary = "Danh sách Phường/Xã theo Quận/Huyện")
    public ResponseEntity<SuccessResponse<List<LocalityResponse>>> getWards(@RequestParam Integer districtId) {
        return ResponseEntity.ok(SuccessResponse.of(shippingService.getWardsByDistrict(districtId)));
    }

    @PostMapping("/fee")
    @Operation(summary = "Tính phí vận chuyển theo GHN")
    public ResponseEntity<SuccessResponse<BigDecimal>> calculateFee(@Valid @RequestBody CalculateShippingFeeRequest request) {
        BigDecimal fee = shippingService.calculateFee(request);
        return ResponseEntity.ok(SuccessResponse.of(fee));
    }
}
