package com.fernirx.sneakerapi.auth.service.impl;

import com.fernirx.sneakerapi.security.UserTokenPayload;
import com.fernirx.sneakerapi.security.oauth2.OAuth2UserInfo;
import com.fernirx.sneakerapi.security.oauth2.OAuth2UserProcessor;
import org.springframework.stereotype.Component;

@Component
public class OAuth2UserProcessorImpl implements OAuth2UserProcessor {
    @Override
    public UserTokenPayload process(OAuth2UserInfo userInfo) {
        return null;
    }
}
