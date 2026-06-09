package com.fernirx.sneakerapi.auth.service.impl;

import com.fernirx.sneakerapi.auth.mapper.AuthMapper;
import com.fernirx.sneakerapi.security.mapper.UserSecurityMapper;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.security.oauth2.OAuth2UserInfo;
import com.fernirx.sneakerapi.security.oauth2.OAuth2UserProcessor;
import com.fernirx.sneakerapi.security.model.UserTokenPayload;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2UserProcessorImpl implements OAuth2UserProcessor {
    private final UserRegistrationService userRegistrationService;
    private final UserSecurityMapper userSecurityMapper;
    private final AuthMapper authMapper;

    @Override
    public UserTokenPayload process(OAuth2UserInfo userInfo) {
        User user = userRegistrationService.findOrCreateOAuth2User(authMapper.toCommand(userInfo));
        CustomUserDetails userDetails = userSecurityMapper.toCustomUserDetails(user);
        return UserTokenPayload.from(userDetails);
    }
}