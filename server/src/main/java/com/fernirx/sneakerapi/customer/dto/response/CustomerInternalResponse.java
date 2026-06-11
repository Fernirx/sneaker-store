package com.fernirx.sneakerapi.customer.dto.response;

import com.fernirx.sneakerapi.customer.enums.MembershipTier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerInternalResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        MembershipTier membershipTier,
        Long loyaltyPoints,
        BigDecimal totalSpent,
        LocalDateTime createdAt
) {}
