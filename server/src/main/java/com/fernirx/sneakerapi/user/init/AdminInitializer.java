package com.fernirx.sneakerapi.user.init;

import com.fernirx.sneakerapi.common.enums.Role;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.entity.UserProfile;
import com.fernirx.sneakerapi.user.entity.UserRole;
import com.fernirx.sneakerapi.user.repository.UserProfileRepository;
import com.fernirx.sneakerapi.user.repository.UserRepository;
import com.fernirx.sneakerapi.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {
    private final AdminProperties adminProperties;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) throws Exception {
        Optional<User> existsUser = userRepository.findByEmailIncludingDeleted(adminProperties.getEmail());
        if (existsUser.isPresent()) {
            User user = existsUser.get();
            if (!passwordEncoder.matches(adminProperties.getPassword(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(adminProperties.getPassword()));
            }
            if (userRoleRepository.findByUserAndRole(user, Role.ROLE_ADMIN).isEmpty()) {
                UserRole userRole = new UserRole();
                userRole.setRole(Role.ROLE_ADMIN);
                userRole.setUser(user);
                userRoleRepository.save(userRole);
            }
            user.setDeletedAt(null);
            user.setActive(true);
        } else {
            User user = User.createByAdmin(
                    adminProperties.getEmail(),
                    passwordEncoder.encode(adminProperties.getPassword())
            );
            userRepository.save(user);

            UserProfile profile = new UserProfile();
            profile.setUser(user);
            profile.setFirstName("Admin");
            userProfileRepository.save(profile);

            UserRole userRole = new UserRole();
            userRole.setRole(Role.ROLE_ADMIN);
            userRole.setUser(user);
            userRoleRepository.save(userRole);
        }
    }
}
