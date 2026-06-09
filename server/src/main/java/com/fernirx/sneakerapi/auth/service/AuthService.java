package com.fernirx.sneakerapi.auth.service;

import com.fernirx.sneakerapi.auth.dto.request.LoginRequest;
import com.fernirx.sneakerapi.auth.dto.request.LogoutRequest;
import com.fernirx.sneakerapi.auth.dto.request.RegisterRequest;
import com.fernirx.sneakerapi.auth.dto.request.VerifyOtpRequest;
import com.fernirx.sneakerapi.security.response.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest request);

    void register(RegisterRequest request);

    TokenResponse verifyOtp(VerifyOtpRequest request);

    void logout(LogoutRequest request);
}
