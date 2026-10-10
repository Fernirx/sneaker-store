package com.fernirx.sneakerapi.user.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.user.dto.request.ChangePasswordRequest;
import com.fernirx.sneakerapi.user.dto.request.SetPasswordRequest;
import com.fernirx.sneakerapi.user.dto.request.UpdateProfileRequest;
import com.fernirx.sneakerapi.user.dto.response.ProfileResponse;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserProfile;
import com.fernirx.sneakerapi.user.mapper.ProfileMapper;
import com.fernirx.sneakerapi.user.repository.UserProfileRepository;
import com.fernirx.sneakerapi.user.repository.UserRepository;
import com.fernirx.sneakerapi.user.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ProfileMapper profileMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Khách hàng lấy thông tin cá nhân của mình.
     */
    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("label.user"));
        return profileMapper.toProfileResponse(user);
    }

    /**
     * Khách hàng cập nhật hồ sơ cá nhân.
     */
    @Override
    @Transactional
    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("label.user"));
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("label.user"));
        profileMapper.updateUserProfile(request, profile);
        userProfileRepository.save(profile);
        user.setUserProfile(profile);
        return profileMapper.toProfileResponse(user);
    }

    /**
     * Khách hàng đổi mật khẩu.
     * Xác thực mật khẩu cũ (currentPassword) trước khi đổi.
     */
    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("label.user"));
        if (user.getPassword() == null
                || !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw BusinessException.bad("label.password");
        }
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    /**
     * Khách hàng (Đăng nhập qua Google/FB) đặt mật khẩu lần đầu để có thể dùng đăng nhập truyền thống.
     * Chặn không cho gọi hàm này nếu user đã có password rồi.
     */
    @Override
    @Transactional
    public void setPassword(Long userId, SetPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("label.user"));
        if (user.getPassword() != null) {
            throw BusinessException.bad("label.password");
        }
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }
}
