package com.fernirx.sneakerapi.security.service;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.security.mapper.UserSecurityMapper;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserSecurityMapper userSecurityMapper;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> BusinessException.notFound("label.user"));
        return userSecurityMapper.toCustomUserDetails(user);
    }
}