package com.fernirx.sneakerapi.user.dto.response;

import com.fernirx.sneakerapi.common.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record UserInternalResponse(
        Long id,
        String email,
        boolean active,
        boolean emailVerified,
        Set<Role> roles,
        LocalDateTime createdAt
) {}
