package com.fernirx.sneakerapi.auth.mapper;

import com.fernirx.sneakerapi.auth.dto.request.RegisterRequest;
import com.fernirx.sneakerapi.security.oauth2.OAuth2UserInfo;
import com.fernirx.sneakerapi.user.dto.command.OAuth2UserCommand;
import com.fernirx.sneakerapi.user.dto.command.RegisterCommand;
import com.fernirx.sneakerapi.user.enums.Provider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING,
        imports = Provider.class
)
public interface AuthMapper {
    @Mapping(target = "provider", expression = "java(Provider.valueOf(info.provider().toUpperCase()))")
    OAuth2UserCommand toCommand(OAuth2UserInfo info);

    RegisterCommand toCommand(RegisterRequest request);
}