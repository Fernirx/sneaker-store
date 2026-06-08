package com.fernirx.sneakerapi.user.mapper;

import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserRole;
import org.mapstruct.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface UserMapper {
    @Mapping(target = "authorities", source = "userRoles", qualifiedByName = "mapAuthorities")
    CustomUserDetails toCustomUserDetails(User user);

    @Named("mapAuthorities")
    default Collection<GrantedAuthority> mapAuthorities(Set<UserRole> roles) {
        return roles.stream()
                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().name()))
                .collect(Collectors.toSet());
    }
}
