package com.fernirx.sneakerapi.user.mapper;

import com.fernirx.sneakerapi.common.enums.Role;
import com.fernirx.sneakerapi.user.dto.response.UserInternalResponse;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface UserMapper {

    @Mapping(target = "emailVerified", expression = "java(user.getVerifiedAt() != null)")
    @Mapping(target = "roles", expression = "java(mapRoles(user.getUserRoles()))")
    UserInternalResponse toInternalResponse(User user);

    default Set<Role> mapRoles(Set<UserRole> userRoles) {
        return userRoles.stream().map(UserRole::getRole).collect(Collectors.toSet());
    }
}
