package com.fernirx.sneakerapi.auth.service.impl;

import com.fernirx.sneakerapi.auth.dto.request.*;
import com.fernirx.sneakerapi.auth.mapper.AuthMapper;
import com.fernirx.sneakerapi.auth.service.AuthService;
import com.fernirx.sneakerapi.auth.service.OtpService;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.security.jwt.JwtProvider;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.security.model.UserTokenPayload;
import com.fernirx.sneakerapi.security.response.TokenResponse;
import com.fernirx.sneakerapi.security.service.TokenBlacklistService;
import com.fernirx.sneakerapi.user.dto.command.RegisterCommand;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.enums.OtpPurpose;
import com.fernirx.sneakerapi.user.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserDetailsService userDetailsService;
    private final UserRegistrationService userRegistrationService;
    private final AuthMapper authMapper;
    private final OtpService otpService;

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
        RegisterCommand command = authMapper.toCommand(request);
        User user = userRegistrationService.createUserWithPassword(command);
        otpService.sendOtp(user.getEmail(), request.firstName(), OtpPurpose.REGISTER);
    }

    @Override
    public TokenResponse verifyOtp(VerifyOtpRequest request) {
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.REGISTER);
        userRegistrationService.verifyEmail(request.email());
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(request.email());
        UserTokenPayload payload = UserTokenPayload.from(userDetails);
        return TokenResponse.builder()
                .accessToken(jwtProvider.generateAccessToken(payload))
                .refreshToken(jwtProvider.generateRefreshToken(payload))
                .build();
    }

    @Override
    public void logout(LogoutRequest request) {
        jwtProvider.validateRefreshToken(request.refreshToken());
        tokenBlacklistService.blacklistRefreshToken(request.refreshToken());
        tokenBlacklistService.blacklistAccessToken(request.accessToken());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        CustomUserDetails userDetails = loadUserOrThrow(request.email());
        if (!userDetails.isEnabled()) {
            throw SecurityCustomException.emailNotVerified();
        }
        otpService.sendOtp(request.email(), request.email().split("@")[0], OtpPurpose.FORGOT_PASSWORD);
    }

    @Override
    public TokenResponse forgotPasswordVerifyOtp(VerifyOtpRequest request) {
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.FORGOT_PASSWORD);
        CustomUserDetails userDetails = loadUserOrThrow(request.email());
        UserTokenPayload payload = UserTokenPayload.from(userDetails);
        return TokenResponse.builder()
                .resetPasswordToken(jwtProvider.generateResetPasswordToken(payload))
                .build();
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        jwtProvider.validateResetPasswordToken(request.resetToken());
        String email = jwtProvider.extractEmail(request.resetToken());
        userRegistrationService.updatePassword(email, request.password());
    }

    @Override
    public void resendOtp(ResendOtpRequest request) {
        CustomUserDetails userDetails = loadUserOrThrow(request.email());
        if (request.purpose() == OtpPurpose.FORGOT_PASSWORD && !userDetails.isEnabled()) {
            throw SecurityCustomException.emailNotVerified();
        }
        otpService.sendOtp(request.email(), request.email().split("@")[0], request.purpose());
    }

    private CustomUserDetails loadUserOrThrow(String email) {
        try {
            return (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            throw BusinessException.notFound("label.email");
        }
    }
}