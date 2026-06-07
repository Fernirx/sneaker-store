package com.fernirx.sneakerapi.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernirx.sneakerapi.common.constant.SecurityConstants;
import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.response.ErrorResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    @Override
    public void handle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType(SecurityConstants.CONTENT_TYPE_JSON);
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                MessageUtil.getMessage(errorCode.getMessageKey())
        );
        response.setStatus(errorCode.getHttpStatus().value());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }
}
