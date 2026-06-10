package com.fernirx.sneakerapi.customer.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.customer.dto.request.CreateAddressRequest;
import com.fernirx.sneakerapi.customer.dto.request.UpdateAddressRequest;
import com.fernirx.sneakerapi.customer.dto.response.AddressResponse;
import com.fernirx.sneakerapi.customer.service.AddressService;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/addresses")
@RequiredArgsConstructor
@Tag(name = "Address API", description = "Quản lý địa chỉ giao hàng")
public class AddressController {
    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Lấy danh sách địa chỉ")
    public ResponseEntity<SuccessResponse<List<AddressResponse>>> getAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AddressResponse> addresses = addressService.getAddresses(userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(addresses));
    }

    @PostMapping
    @Operation(summary = "Thêm địa chỉ mới")
    public ResponseEntity<SuccessResponse<AddressResponse>> createAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateAddressRequest request) {
        AddressResponse response = addressService.createAddress(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of("success.address.create", response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Cập nhật địa chỉ")
    public ResponseEntity<SuccessResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAddressRequest request) {
        AddressResponse response = addressService.updateAddress(userDetails.getId(), id, request);
        return ResponseEntity.ok(SuccessResponse.of("success.address.update", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa địa chỉ")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        addressService.deleteAddress(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
