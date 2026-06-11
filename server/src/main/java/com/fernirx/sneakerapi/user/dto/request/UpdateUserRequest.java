package com.fernirx.sneakerapi.user.dto.request;

import com.fernirx.sneakerapi.common.enums.Role;

import java.util.Set;

public record UpdateUserRequest(
        Boolean active,
        Set<Role> roles
) {}
