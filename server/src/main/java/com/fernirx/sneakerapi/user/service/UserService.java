package com.fernirx.sneakerapi.user.service;

import com.fernirx.sneakerapi.user.dto.request.CreateUserRequest;
import com.fernirx.sneakerapi.user.dto.request.UpdateUserRequest;
import com.fernirx.sneakerapi.user.dto.request.UserFilterRequest;
import com.fernirx.sneakerapi.user.dto.response.UserInternalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserInternalResponse> getUsers(UserFilterRequest filter, Pageable pageable);
    UserInternalResponse getUserById(Long id);
    UserInternalResponse createUser(CreateUserRequest request);
    UserInternalResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
}
