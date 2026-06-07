package com.fernirx.sneakerapi.security.oauth2;

import com.fernirx.sneakerapi.security.model.UserTokenPayload;

public interface OAuth2UserProcessor {
    UserTokenPayload process(OAuth2UserInfo userInfo);
}
