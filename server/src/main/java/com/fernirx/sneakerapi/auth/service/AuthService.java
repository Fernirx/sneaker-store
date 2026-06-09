package com.fernirx.sneakerapi.auth.service;

import com.fernirx.sneakerapi.auth.dto.request.*;
import com.fernirx.sneakerapi.security.response.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void register(RegisterRequest request);

    TokenResponse verifyOtp(VerifyOtpRequest request);

    void logout(LogoutRequest request);
}
