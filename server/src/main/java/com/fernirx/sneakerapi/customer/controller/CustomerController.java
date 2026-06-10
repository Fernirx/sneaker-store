package com.fernirx.sneakerapi.customer.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.customer.dto.response.CustomerResponse;
import com.fernirx.sneakerapi.customer.service.CustomerService;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/customer")
@RequiredArgsConstructor
@Tag(name = "Customer API", description = "Thông tin khách hàng")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "Lấy thông tin khách hàng")
    public ResponseEntity<SuccessResponse<CustomerResponse>> getCustomer(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CustomerResponse response = customerService.getCustomer(userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(response));
    }
}
