package com.fernirx.sneakerapi.auth.service.impl;

import com.fernirx.sneakerapi.auth.dto.request.LoginRequest;
import com.fernirx.sneakerapi.auth.dto.request.LogoutRequest;
import com.fernirx.sneakerapi.auth.dto.request.RegisterRequest;
import com.fernirx.sneakerapi.auth.dto.request.VerifyOtpRequest;
import com.fernirx.sneakerapi.auth.service.AuthService;
import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.security.jwt.JwtProvider;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.security.model.UserTokenPayload;
import com.fernirx.sneakerapi.security.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
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
    private final PasswordEncoder passwordEncoder;

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