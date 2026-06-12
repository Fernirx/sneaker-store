package com.fernirx.sneakerapi.user.service;

import com.fernirx.sneakerapi.user.dto.request.ChangePasswordRequest;
import com.fernirx.sneakerapi.user.dto.request.SetPasswordRequest;
import com.fernirx.sneakerapi.user.dto.request.UpdateProfileRequest;
import com.fernirx.sneakerapi.user.dto.response.ProfileResponse;

public interface ProfileService {
    ProfileResponse getMe(Long userId);
    ProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    void setPassword(Long userId, SetPasswordRequest request);
}