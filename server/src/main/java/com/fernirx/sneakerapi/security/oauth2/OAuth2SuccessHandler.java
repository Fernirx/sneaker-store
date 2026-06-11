package com.fernirx.sneakerapi.security.oauth2;

import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.security.jwt.JwtProvider;
import com.fernirx.sneakerapi.security.model.UserTokenPayload;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final OAuth2UserProcessor oAuth2UserProcessor;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication) throws IOException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            redirectError(response, "oauth2_failed");
            return;
        }
        String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfo.from(oAuth2User, provider);
        UserTokenPayload userTokenPayload;
        try {
            userTokenPayload = oAuth2UserProcessor.process(userInfo);
        } catch (SecurityCustomException ex) {
            redirectError(response, ex.getErrorCode().name());
            return;
        }
        if (userTokenPayload == null) {
            redirectError(response, "oauth2_failed");
            return;
        }
        String accessToken = jwtProvider.generateAccessToken(userTokenPayload);
        String refreshToken = jwtProvider.generateRefreshToken(userTokenPayload);
        clearAuthenticationAttributes(request);
        response.sendRedirect(frontendUrl + "/api/auth/oauth2/callback"
                + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8));
    }

    private void redirectError(HttpServletResponse response, String error) throws IOException {
        response.sendRedirect(frontendUrl + "/login?error=" + URLEncoder.encode(error, StandardCharsets.UTF_8));
    }
}
