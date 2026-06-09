package com.fernirx.sneakerapi.user.mapper;

import com.fernirx.sneakerapi.user.dto.command.OAuth2UserCommand;
import com.fernirx.sneakerapi.user.dto.command.RegisterCommand;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserOauth;
import com.fernirx.sneakerapi.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface UserRegistrationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "userOauths", ignore = true)
    @Mapping(target = "userProfile", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(OAuth2UserCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "avatarPublicId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserProfile toUserProfile(OAuth2UserCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UserOauth toUserOauth(OAuth2UserCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "verifiedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "userOauths", ignore = true)
    @Mapping(target = "userProfile", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(RegisterCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "avatarPublicId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserProfile toUserProfile(RegisterCommand command);
}