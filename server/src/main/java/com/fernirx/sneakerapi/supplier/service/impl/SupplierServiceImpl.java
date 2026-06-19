package com.fernirx.sneakerapi.supplier.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.supplier.dto.request.CreateSupplierRequest;
import com.fernirx.sneakerapi.supplier.dto.request.SupplierFilterRequest;
import com.fernirx.sneakerapi.supplier.dto.request.UpdateSupplierRequest;
import com.fernirx.sneakerapi.supplier.dto.response.SupplierResponse;
import com.fernirx.sneakerapi.supplier.entity.Supplier;
import com.fernirx.sneakerapi.supplier.mapper.SupplierMapper;
import com.fernirx.sneakerapi.supplier.repository.SupplierRepository;
import com.fernirx.sneakerapi.supplier.repository.SupplierSpec;
import com.fernirx.sneakerapi.supplier.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAll(SupplierFilterRequest filter, Pageable pageable) {
        return supplierRepository.findAll(SupplierSpec.build(filter), pageable)
                .map(supplierMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getById(Long id) {
        return supplierMapper.toResponse(findById(id));
    }

    @Override
    public SupplierResponse create(CreateSupplierRequest request) {
        if (supplierRepository.existsByCodeIgnoreCase(request.code())) {
            throw BusinessException.alreadyExists("label.supplier");
        }
        if (supplierRepository.existsByNameIgnoreCase(request.name())) {
            throw BusinessException.alreadyExists("label.supplier");
        }
        Supplier supplier = new Supplier();
        supplier.setCode(request.code().toUpperCase());
        supplier.setName(request.name());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setContactPerson(request.contactPerson());
        supplier.setContactPhone(request.contactPhone());
        supplier.setAddress(request.address());
        supplier.setNotes(request.notes());
        supplier.setActive(true);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    public SupplierResponse update(Long id, UpdateSupplierRequest request) {
        Supplier supplier = findById(id);
        if (request.code() != null && !request.code().equalsIgnoreCase(supplier.getCode())
                && supplierRepository.existsByCodeIgnoreCase(request.code())) {
            throw BusinessException.alreadyExists("label.supplier");
        }
        if (request.name() != null && !request.name().equalsIgnoreCase(supplier.getName())
                && supplierRepository.existsByNameIgnoreCase(request.name())) {
            throw BusinessException.alreadyExists("label.supplier");
        }
        supplierMapper.updateSupplier(request, supplier);
        if (request.code() != null) {
            supplier.setCode(request.code().toUpperCase());
        }
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    public void delete(Long id) {
        Supplier supplier = findById(id);
        if (!supplier.getPurchases().isEmpty()) {
            throw BusinessException.inUse("label.supplier");
        }
        supplierRepository.delete(supplier);
    }

    private Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.supplier"));
    }
}
