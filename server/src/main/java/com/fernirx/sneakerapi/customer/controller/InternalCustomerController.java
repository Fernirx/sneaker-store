package com.fernirx.sneakerapi.customer.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.customer.dto.request.UpdateCustomerRequest;
import com.fernirx.sneakerapi.customer.dto.request.CustomerFilterRequest;
import com.fernirx.sneakerapi.customer.dto.response.CustomerInternalResponse;
import com.fernirx.sneakerapi.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/customers")
@RequiredArgsConstructor
@Tag(name = "Internal - Customer", description = "Quản lý khách hàng (nội bộ)")
public class InternalCustomerController {
    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Danh sách khách hàng")
    public ResponseEntity<PageResponse<CustomerInternalResponse>> getCustomers(
            @ParameterObject @ModelAttribute CustomerFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(customerService.getCustomers(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Chi tiết khách hàng")
    public ResponseEntity<SuccessResponse<CustomerInternalResponse>> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(customerService.getCustomerById(id)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật khách hàng")
    public ResponseEntity<SuccessResponse<CustomerInternalResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.customer")),
                customerService.updateCustomer(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa khách hàng")
    public ResponseEntity<SuccessResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.customer"))
        ));
    }
}
