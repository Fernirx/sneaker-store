package com.fernirx.sneakerapi.auth.controller;

import com.fernirx.sneakerapi.auth.dto.request.LoginRequest;
import com.fernirx.sneakerapi.auth.dto.request.RefreshTokenRequest;
import com.fernirx.sneakerapi.auth.service.AuthService;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.security.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
