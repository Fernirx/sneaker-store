package com.fernirx.sneakerapi.user.dto.command;

import com.fernirx.sneakerapi.user.enums.Provider;

public record OAuth2UserCommand(
        String email,
        String firstName,
        String lastName,
        Provider provider,
        String providerId
) {}