package com.fernirx.sneakerapi.auth.controller;

import com.fernirx.sneakerapi.auth.dto.request.*;
import com.fernirx.sneakerapi.auth.service.AuthService;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.security.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "Các API xác thực người dùng")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Đăng nhập",
            description = "Xác thực người dùng bằng email và mật khẩu, trả về access token và refresh token"
    )
    public ResponseEntity<SuccessResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.login"),
                tokenResponse
        ));
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Làm mới token",
            description = "Cấp mới access token và refresh token từ refresh token hợp lệ"
    )
    public ResponseEntity<SuccessResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        TokenResponse tokenResponse = authService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.refresh"),
                tokenResponse
        ));
    }

    @PostMapping("/register")
    @Operation(
            summary = "Đăng ký tài khoản",
            description = "Tạo tài khoản mới và gửi OTP xác minh email"
    )
    public ResponseEntity<SuccessResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.register"), null
        ));
    }

    @PostMapping("/verify-otp")
    @Operation(
            summary = "Xác minh OTP",
            description = "Xác minh email sau đăng ký, trả về access token và refresh token"
    )
    public ResponseEntity<SuccessResponse<TokenResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        TokenResponse tokenResponse = authService.verifyOtp(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.verify_otp"),
                tokenResponse
        ));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Đăng xuất",
            description = "Vô hiệu hóa refresh token hiện tại"
    )
    public ResponseEntity<SuccessResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.logout"), null
        ));
    }
}