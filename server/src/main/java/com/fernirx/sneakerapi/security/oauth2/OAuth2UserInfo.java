package com.fernirx.sneakerapi.security.oauth2;

public record OAuth2UserInfo(
        String email,
        String firstName,
        String lastName,
        String provider,
        String providerId
) {}