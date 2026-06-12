package com.fernirx.sneakerapi.user.dto.request;

import com.fernirx.sneakerapi.common.enums.Role;

public record UserFilterRequest(
        String search,
        Role role,
        Boolean active
) {}
