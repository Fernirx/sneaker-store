package com.fernirx.sneakerapi.supplier.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.supplier.dto.request.CreateSupplierRequest;
import com.fernirx.sneakerapi.supplier.dto.request.SupplierFilterRequest;
import com.fernirx.sneakerapi.supplier.dto.request.UpdateSupplierRequest;
import com.fernirx.sneakerapi.supplier.dto.response.SupplierResponse;
import com.fernirx.sneakerapi.supplier.service.SupplierService;
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
@RequestMapping("/internal/suppliers")
@RequiredArgsConstructor
@Tag(name = "Internal Supplier API", description = "Quản lý nhà cung cấp (nội bộ)")
public class InternalSupplierController {
    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Danh sách nhà cung cấp")
    public ResponseEntity<PageResponse<SupplierResponse>> getAll(
            @ParameterObject @ModelAttribute SupplierFilterRequest filter,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(supplierService.getAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Chi tiết nhà cung cấp")
    public ResponseEntity<SuccessResponse<SupplierResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(supplierService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Tạo nhà cung cấp mới")
    public ResponseEntity<SuccessResponse<SupplierResponse>> create(
            @Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse response = supplierService.create(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.supplier")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE')")
    @Operation(summary = "Cập nhật nhà cung cấp")
    public ResponseEntity<SuccessResponse<SupplierResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierResponse response = supplierService.update(id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.supplier")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa nhà cung cấp")
    public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.supplier"))
        ));
    }
}
