package com.fernirx.sneakerapi.security.filter;

import com.fernirx.sneakerapi.common.constant.SecurityConstants;
import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.common.utils.RedisKeyUtils;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;
    private final StringRedisTemplate redisTemplate;
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            UserDetailsService userDetailsService,
            StringRedisTemplate redisTemplate,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
        this.redisTemplate = redisTemplate;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = extractJwtToken(request);
        if (token != null) {
            try {
                jwtProvider.validateAccessToken(token);
                String jti = jwtProvider.extractJti(token);
                String blacklistKey = RedisKeyUtils.revokedAccessKey(jti);
                if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                    throw SecurityCustomException.invalid("label.token");
                }
                setAuthenticationContext(token, request);
            } catch (Exception e) {
                resolver.resolveException(request, response, null, e);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractJwtToken(HttpServletRequest request) {
        String headerAuth = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return headerAuth.substring(SecurityConstants.BEARER_PREFIX_LENGTH);
        }
        return null;
    }

    private void setAuthenticationContext(String token, HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = jwtProvider.extractEmail(token);
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}
