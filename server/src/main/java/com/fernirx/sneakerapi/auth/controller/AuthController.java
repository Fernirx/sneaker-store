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
    @Operation(summary = "Đăng nhập", description = "Xác thực bằng email và mật khẩu, trả về access token và refresh token")
    public ResponseEntity<SuccessResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.login"),
                tokenResponse
        ));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Làm mới token", description = "Cấp mới access token và refresh token từ refresh token hợp lệ")
    public ResponseEntity<SuccessResponse<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = authService.refreshToken(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.refresh"),
                tokenResponse
        ));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo tài khoản mới và gửi OTP xác minh email")
    public ResponseEntity<SuccessResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.register")
        ));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Xác minh OTP đăng ký", description = "Xác minh email sau đăng ký, trả về access token và refresh token")
    public ResponseEntity<SuccessResponse<TokenResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        TokenResponse tokenResponse = authService.verifyOtp(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.verify_otp"),
                tokenResponse
        ));
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Gửi lại OTP", description = "Gửi lại mã OTP cho xác minh email hoặc quên mật khẩu")
    public ResponseEntity<SuccessResponse<Void>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {
        authService.resendOtp(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.resend_otp")
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", description = "Vô hiệu hóa access token và refresh token hiện tại")
    public ResponseEntity<SuccessResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.logout")
        ));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Gửi OTP đặt lại mật khẩu đến email nếu tài khoản tồn tại và đã xác minh")
    public ResponseEntity<SuccessResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.forgot_password")
        ));
    }

    @PostMapping("/forgot-password/verify-otp")
    @Operation(summary = "Xác minh OTP quên mật khẩu", description = "Xác minh OTP và nhận reset token để đặt lại mật khẩu")
    public ResponseEntity<SuccessResponse<TokenResponse>> forgotPasswordVerifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        TokenResponse response = authService.forgotPasswordVerifyOtp(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.forgot_password.verify_otp"),
                response
        ));
    }

    @PostMapping("/forgot-password/reset")
    @Operation(summary = "Đặt lại mật khẩu", description = "Đặt mật khẩu mới bằng reset token từ bước xác minh OTP")
    public ResponseEntity<SuccessResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.auth.reset_password"), null
        ));
    }
}