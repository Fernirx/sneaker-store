package com.fernirx.sneakerapi.user.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.user.dto.request.ChangePasswordRequest;
import com.fernirx.sneakerapi.user.dto.request.UpdateProfileRequest;
import com.fernirx.sneakerapi.user.dto.response.ProfileResponse;
import com.fernirx.sneakerapi.user.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
@Tag(name = "Me API", description = "Thông tin tài khoản hiện tại")
public class MeController {
    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Lấy thông tin cá nhân")
    public ResponseEntity<SuccessResponse<ProfileResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileResponse response = profileService.getMe(userDetails.getId());
        return ResponseEntity.ok(SuccessResponse.of(response));
    }

    @PatchMapping
    @Operation(summary = "Cập nhật thông tin cá nhân")
    public ResponseEntity<SuccessResponse<ProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        ProfileResponse response = profileService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(SuccessResponse.of("success.profile.update", response));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu")
    public ResponseEntity<SuccessResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(userDetails.getId(), request);
        return ResponseEntity.ok(SuccessResponse.of("success.profile.change_password"));
    }
}
