package com.fernirx.sneakerapi.customer.dto.request;

import com.fernirx.sneakerapi.customer.enums.MembershipTier;

public record CustomerFilterRequest(
        String search,
        MembershipTier membershipTier
) {}
