package com.fernirx.sneakerapi.customer.dto.response;

import com.fernirx.sneakerapi.customer.enums.MembershipTier;

import java.math.BigDecimal;

public record CustomerResponse(
        MembershipTier membershipTier,
        Long loyaltyPoints,
        BigDecimal totalSpent
) {}
