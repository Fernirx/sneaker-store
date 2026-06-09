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
import com.fernirx.sneakerapi.user.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserRegistrationServiceImpl implements UserRegistrationService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserOauthRepository userOauthRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRegistrationMapper userRegistrationMapper;
    private final PasswordEncoder passwordEncoder;

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
        userProfileRepository.save(profile);

        UserRole role = new UserRole();
        role.setUser(user);
        role.setRole(Role.ROLE_USER);
        userRoleRepository.save(role);
        return user;
    }

    @Override
    @Transactional
    public void verifyEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BusinessException.notFound("label.user"));
        user.setVerifiedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BusinessException.notFound("label.user"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private User linkProviderIfAbsent(User user, OAuth2UserCommand command) {
        if (!userOauthRepository.existsByProviderAndProviderId(command.provider(), command.providerId())) {
            UserOauth oauth = userRegistrationMapper.toUserOauth(command);
            oauth.setUser(user);
            userOauthRepository.save(oauth);
        }
        Hibernate.initialize(user.getUserRoles());
        return user;
    }

    private User createOAuth2User(OAuth2UserCommand command) {
        User user = userRegistrationMapper.toUser(command);
        user.setVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        UserProfile profile = userRegistrationMapper.toUserProfile(command);
        profile.setUser(user);
        userProfileRepository.save(profile);

        UserRole role = new UserRole();
        role.setUser(user);
        role.setRole(Role.ROLE_USER);
        userRoleRepository.save(role);
        user.getUserRoles().add(role);

        UserOauth oauth = userRegistrationMapper.toUserOauth(command);
        oauth.setUser(user);
        userOauthRepository.save(oauth);

        return user;
    }
}