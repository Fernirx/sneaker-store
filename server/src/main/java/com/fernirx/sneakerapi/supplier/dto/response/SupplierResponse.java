package com.fernirx.sneakerapi.supplier.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SupplierResponse(
        Long id,
        String code,
        String name,
        String email,
        String phone,
        String contactPerson,
        String contactPhone,
        String address,
        String notes,
        boolean active,
        LocalDateTime createdAt
) {}
