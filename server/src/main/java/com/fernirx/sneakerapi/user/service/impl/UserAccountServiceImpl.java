package com.fernirx.sneakerapi.user.service.impl;

import com.fernirx.sneakerapi.common.enums.Role;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.user.dto.command.RegisterCommand;
import com.fernirx.sneakerapi.user.dto.command.OAuth2UserCommand;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserOauth;
import com.fernirx.sneakerapi.user.entity.UserProfile;
import com.fernirx.sneakerapi.user.entity.UserRole;
import com.fernirx.sneakerapi.user.mapper.UserRegistrationMapper;
import com.fernirx.sneakerapi.user.repository.UserOauthRepository;
import com.fernirx.sneakerapi.user.repository.UserProfileRepository;
import com.fernirx.sneakerapi.user.repository.UserRepository;
import com.fernirx.sneakerapi.user.repository.UserRoleRepository;
import com.fernirx.sneakerapi.user.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserOauthRepository userOauthRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRegistrationMapper userRegistrationMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Xử lý đăng nhập qua mạng xã hội (OAuth2).
     * Logic bảo vệ:
     * 1. Tìm user bằng Email (kể cả account đã xóa mềm).
     * 2. Nếu account đã xóa mềm -> Chặn đăng nhập (Account Unavailable). Tránh lỗ hổng user bị ban mượn Google để vượt rào.
     * 3. Nếu account tồn tại bình thường -> Liên kết (link) Provider nếu chưa có.
     * 4. Nếu chưa tồn tại -> Tạo mới (Auto-register).
     */
    @Override
    @Transactional
    public User findOrCreateOAuth2User(OAuth2UserCommand command) {
        return userRepository.findByEmailIncludingDeleted(command.email())
                .map(user -> {
                    if (user.getDeletedAt() != null) {
                        throw SecurityCustomException.accountUnavailable();
                    }
                    return linkProviderIfAbsent(user, command);
                })
                .orElseGet(() -> createOAuth2User(command));
    }

    /**
     * Đăng ký tài khoản truyền thống (Email/Password).
     * Kiểm tra tương tự như OAuth2: Chặn đăng ký đè lên account đã xóa mềm hoặc account đang hoạt động.
     */
    @Override
    public User createUserWithPassword(RegisterCommand command) {
        Optional<User> existingUserOpt = userRepository.findByEmailIncludingDeleted(command.email());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.getDeletedAt() != null) {
                throw SecurityCustomException.accountUnavailable();
            }
            throw BusinessException.alreadyExists("label.email");
        }
        User user = userRegistrationMapper.toUser(command);
        user.setPassword(passwordEncoder.encode(command.password()));
        userRepository.save(user);

        UserProfile profile = userRegistrationMapper.toUserProfile(command);
        profile.setUser(user);
        userProfileRepository.save(profile);

        UserRole role = new UserRole();
        role.setUser(user);
        role.setRole(Role.ROLE_CUSTOMER);
        userRoleRepository.save(role);
        return user;
    }

    /**
     * Kích hoạt tài khoản (Sau khi bấm link trong Email).
     */
    @Override
    @Transactional
    public void verifyEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BusinessException.notFound("label.user"));
        user.setVerifiedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Quên mật khẩu (Reset password).
     */
    @Override
    @Transactional
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BusinessException.notFound("label.user"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Helper liên kết tài khoản mạng xã hội vào tài khoản đã có sẵn.
     */
    private User linkProviderIfAbsent(User user, OAuth2UserCommand command) {
        if (!userOauthRepository.existsByProviderAndProviderId(command.provider(), command.providerId())) {
            UserOauth oauth = userRegistrationMapper.toUserOauth(command);
            oauth.setUser(user);
            userOauthRepository.save(oauth);
        }
        Hibernate.initialize(user.getUserRoles());
        return user;
    }

    /**
     * Helper tạo tài khoản mới từ dữ liệu của Google/Facebook.
     * Auto set VerifiedAt = Now (Vì Google/FB đã xác thực email giùm rồi).
     */
    private User createOAuth2User(OAuth2UserCommand command) {
        User user = userRegistrationMapper.toUser(command);
        user.setVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        UserProfile profile = userRegistrationMapper.toUserProfile(command);
        profile.setUser(user);
        userProfileRepository.save(profile);

        UserRole role = new UserRole();
        role.setUser(user);
        role.setRole(Role.ROLE_CUSTOMER);
        userRoleRepository.save(role);
        user.getUserRoles().add(role);

        UserOauth oauth = userRegistrationMapper.toUserOauth(command);
        oauth.setUser(user);
        userOauthRepository.save(oauth);

        return user;
    }
}
