package com.fernirx.sneakerapi.auth.service.impl;

import com.fernirx.sneakerapi.auth.dto.request.*;
import com.fernirx.sneakerapi.auth.service.AuthService;
import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.security.jwt.JwtProvider;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.security.model.UserTokenPayload;
import com.fernirx.sneakerapi.security.response.TokenResponse;
import com.fernirx.sneakerapi.security.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Override
    public TokenResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw SecurityCustomException.invalidCredentials();
        } catch (LockedException e) {
            throw SecurityCustomException.accountUnavailable();
        } catch (DisabledException e) {
            throw SecurityCustomException.emailNotVerified();
        }
        UserTokenPayload payload = UserTokenPayload.from((CustomUserDetails) authentication.getPrincipal());
        String accessToken = jwtProvider.generateAccessToken(payload);
        String refreshToken = jwtProvider.generateRefreshToken(payload);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String oldRefreshToken = request.refreshToken();
        jwtProvider.validateRefreshToken(oldRefreshToken);
        if (tokenBlacklistService.isRefreshTokenBlacklisted(oldRefreshToken)) {
            throw SecurityCustomException.invalid("label.token");
        }
        String email = jwtProvider.extractEmail(oldRefreshToken);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        UserTokenPayload payload = UserTokenPayload.from(userDetails);
        String accessToken = jwtProvider.generateAccessToken(payload);
        String newRefreshToken = jwtProvider.generateRefreshToken(payload);
        tokenBlacklistService.blacklistRefreshToken(oldRefreshToken);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void register(RegisterRequest request) {

    }

    @Override
    public TokenResponse verifyOtp(VerifyOtpRequest request) {
        return null;
    }

    @Override
    public void logout(LogoutRequest request) {

    }
}