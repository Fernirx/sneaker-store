package com.fernirx.sneakerapi.auth.service.impl;

import com.fernirx.sneakerapi.auth.mapper.AuthMapper;
import com.fernirx.sneakerapi.customer.service.CustomerService;
import com.fernirx.sneakerapi.security.mapper.UserSecurityMapper;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.security.oauth2.OAuth2UserInfo;
import com.fernirx.sneakerapi.security.oauth2.OAuth2UserProcessor;
import com.fernirx.sneakerapi.security.model.UserTokenPayload;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2UserProcessorImpl implements OAuth2UserProcessor {
    private final UserAccountService userAccountService;
    private final CustomerService customerService;
    private final UserSecurityMapper userSecurityMapper;
    private final AuthMapper authMapper;

    @Override
    public UserTokenPayload process(OAuth2UserInfo userInfo) {
        User user = userAccountService.findOrCreateOAuth2User(authMapper.toCommand(userInfo));
        customerService.initCustomer(user);
        CustomUserDetails userDetails = userSecurityMapper.toCustomUserDetails(user);
        return UserTokenPayload.from(userDetails);
    }
}