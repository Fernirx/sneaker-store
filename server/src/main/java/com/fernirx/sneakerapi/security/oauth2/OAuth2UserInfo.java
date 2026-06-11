package com.fernirx.sneakerapi.security.oauth2;

import org.springframework.security.oauth2.core.user.OAuth2User;

public record OAuth2UserInfo(
        String email,
        String firstName,
        String lastName,
        String provider,
        String providerId
) {
    public static OAuth2UserInfo from(OAuth2User oAuth2User, String provider) {
        return switch (provider) {
            case "google" -> new OAuth2UserInfo(
                    oAuth2User.getAttribute("email"),
                    oAuth2User.getAttribute("given_name"),
                    oAuth2User.getAttribute("family_name"),
                    provider,
                    oAuth2User.getAttribute("sub")
            );
            case "facebook" -> new OAuth2UserInfo(
                    oAuth2User.getAttribute("email"),
                    oAuth2User.getAttribute("first_name"),
                    oAuth2User.getAttribute("last_name"),
                    provider,
                    oAuth2User.getAttribute("id")
            );
            default -> throw new IllegalStateException("Unsupported OAuth2 provider: " + provider);
        };
    }
}