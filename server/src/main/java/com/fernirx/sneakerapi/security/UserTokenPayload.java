package com.fernirx.sneakerapi.security;

import java.util.Objects;
import java.util.Set;

public record UserTokenPayload(
        Long id,
        String email,
        Set<String> authorities
) {
    public UserTokenPayload {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(email, "email must not be null");
    }

    public static UserTokenPayload from(CustomUserDetails userDetails) {
        return new UserTokenPayload(
                userDetails.getId(),
                userDetails.getEmail(),
                SecurityUtils.getAuthorities(userDetails)
        );
    }
}
