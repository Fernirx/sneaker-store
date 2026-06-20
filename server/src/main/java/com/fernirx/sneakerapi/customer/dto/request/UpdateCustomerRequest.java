package com.fernirx.sneakerapi.customer.dto.request;

import com.fernirx.sneakerapi.customer.enums.MembershipTier;
import jakarta.validation.constraints.Min;

public record UpdateCustomerRequest(
        @Min(value = 0, message = "{validation.number.min}")
        Long loyaltyPoints,

        MembershipTier membershipTier
) {}
