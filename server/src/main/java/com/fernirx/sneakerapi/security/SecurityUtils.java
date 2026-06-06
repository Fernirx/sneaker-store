package com.fernirx.sneakerapi.security;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class SecurityUtils {
    public Set<String> getAuthorities(CustomUserDetails user) {
        if (user == null) return Collections.emptySet();

        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
