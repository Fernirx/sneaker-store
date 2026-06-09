package com.fernirx.sneakerapi.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.common.response.ErrorResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.security.jwt.JwtProvider;
import com.fernirx.sneakerapi.security.model.UserTokenPayload;
import com.fernirx.sneakerapi.security.response.TokenResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final OAuth2UserProcessor oAuth2UserProcessor;

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        if (!(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            response.setStatus(ErrorCode.UNAUTHORIZED.getHttpStatus().value());
            objectMapper.writeValue(
                    response.getWriter(),
                    ErrorResponse.of(ErrorCode.UNAUTHORIZED, MessageUtil.getMessage("error.auth.oauth2_failed"))
            );
            return;
        }
        String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        OAuth2UserInfo userInfo = new OAuth2UserInfo(
                oAuth2User.getAttribute("email"),
                oAuth2User.getAttribute("given_name"),
                oAuth2User.getAttribute("family_name"),
                provider,
                oAuth2User.getAttribute("sub")
        );
        UserTokenPayload userTokenPayload;
        try {
            userTokenPayload = oAuth2UserProcessor.process(userInfo);
        } catch (SecurityCustomException ex) {
            response.setStatus(ex.getErrorCode().getHttpStatus().value());
            objectMapper.writeValue(
                    response.getWriter(),
                    ErrorResponse.of(ex.getErrorCode(), MessageUtil.getMessage(ex.getErrorCode().getMessageKey(), ex.getArgs()))
            );
            return;
        }
        if (userTokenPayload == null) {
            response.setStatus(ErrorCode.UNAUTHORIZED.getHttpStatus().value());
            objectMapper.writeValue(
                    response.getWriter(),
                    ErrorResponse.of(ErrorCode.UNAUTHORIZED, MessageUtil.getMessage("error.auth.oauth2_failed"))
            );
            return;
        }
        String accessToken = jwtProvider.generateAccessToken(userTokenPayload);
        String refreshToken = jwtProvider.generateRefreshToken(userTokenPayload);
        clearAuthenticationAttributes(request);
        objectMapper.writeValue(response.getWriter(), TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build());
    }
}
