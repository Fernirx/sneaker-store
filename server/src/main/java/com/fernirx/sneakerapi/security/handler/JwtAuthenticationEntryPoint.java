package com.fernirx.sneakerapi.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernirx.sneakerapi.common.constant.SecurityConstants;
import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.response.ErrorResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(@NonNull HttpServletRequest request,
                         HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException, ServletException {
        response.setContentType(SecurityConstants.CONTENT_TYPE_JSON);
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED,
                MessageUtil.getMessage("error.auth.unauthenticated")
        );
        response.setStatus(ErrorCode.UNAUTHORIZED.getHttpStatus().value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}