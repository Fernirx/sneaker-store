package com.fernirx.sneakerapi.user.service.impl;

import com.fernirx.sneakerapi.common.enums.Role;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.user.dto.request.CreateUserRequest;
import com.fernirx.sneakerapi.user.dto.request.UpdateUserRequest;
import com.fernirx.sneakerapi.user.dto.request.UserFilterRequest;
import com.fernirx.sneakerapi.user.dto.response.UserInternalResponse;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserProfile;
import com.fernirx.sneakerapi.user.entity.UserRole;
import com.fernirx.sneakerapi.user.mapper.UserMapper;
import com.fernirx.sneakerapi.user.repository.UserProfileRepository;
import com.fernirx.sneakerapi.user.repository.UserRepository;
import com.fernirx.sneakerapi.user.repository.UserRoleRepository;
import com.fernirx.sneakerapi.user.repository.UserSpec;
import com.fernirx.sneakerapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserInternalResponse> getUsers(UserFilterRequest filter, Pageable pageable) {
        return userRepository.findAll(UserSpec.build(filter), pageable)
                .map(userMapper::toInternalResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInternalResponse getUserById(Long id) {
        return userMapper.toInternalResponse(findById(id));
    }

    @Override
    public UserInternalResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmailIncludingDeleted(request.email()).isPresent()) {
            throw BusinessException.alreadyExists("label.email");
        }

        User user = User.createByAdmin(
                request.email(),
                passwordEncoder.encode(request.password())
        );
        userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFirstName(request.firstName());
        userProfileRepository.save(profile);

        Set<Role> roles = (request.roles() != null && !request.roles().isEmpty())
                ? request.roles()
                : Set.of(Role.ROLE_USER);
        roles.forEach(role -> {
            UserRole ur = new UserRole();
            ur.setUser(user);
            ur.setRole(role);
            userRoleRepository.save(ur);
        });

        return userMapper.toInternalResponse(userRepository.findById(user.getId()).orElseThrow());
    }

    @Override
    public UserInternalResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findById(id);
        if (request.active() != null) {
            user.setActive(request.active());
        }
        if (request.roles() != null) {
            userRoleRepository.deleteAllByUser(user);
            userRoleRepository.flush();
            request.roles().forEach(role -> {
                UserRole ur = new UserRole();
                ur.setUser(user);
                ur.setRole(role);
                userRoleRepository.save(ur);
            });
        }
        userRepository.save(user);
        return userMapper.toInternalResponse(userRepository.findById(id).orElseThrow());
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.delete(findById(id));
    }

    private User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.user"));
    }
}
