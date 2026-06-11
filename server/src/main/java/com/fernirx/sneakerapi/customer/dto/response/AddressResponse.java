package com.fernirx.sneakerapi.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AddressResponse(
        Long id,
        String name,
        String phone,
        String street,
        String ward,
        String district,
        String province,
        String postalCode,
        Boolean defaultAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
