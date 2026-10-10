package com.fernirx.sneakerapi.supplier.service;

import com.fernirx.sneakerapi.supplier.dto.request.CreateSupplierRequest;
import com.fernirx.sneakerapi.supplier.dto.request.SupplierFilterRequest;
import com.fernirx.sneakerapi.supplier.dto.request.UpdateSupplierRequest;
import com.fernirx.sneakerapi.supplier.dto.response.SupplierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {
    Page<SupplierResponse> getAll(SupplierFilterRequest filter, Pageable pageable);
    SupplierResponse getById(Long id);
    SupplierResponse create(CreateSupplierRequest request);
    SupplierResponse update(Long id, UpdateSupplierRequest request);
    void delete(Long id);
}
