package com.fernirx.sneakerapi.user.service;

import com.fernirx.sneakerapi.user.dto.command.OAuth2UserCommand;
import com.fernirx.sneakerapi.user.entity.User;

public interface UserRegistrationService {
    User findOrCreateOAuth2User(OAuth2UserCommand command);
}