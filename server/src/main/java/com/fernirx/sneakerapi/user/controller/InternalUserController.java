package com.fernirx.sneakerapi.user.controller;

import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.user.dto.request.CreateUserRequest;
import com.fernirx.sneakerapi.user.dto.request.UpdateUserRequest;
import com.fernirx.sneakerapi.user.dto.request.UserFilterRequest;
import com.fernirx.sneakerapi.user.dto.response.UserInternalResponse;
import com.fernirx.sneakerapi.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Tag(name = "Internal User API", description = "Quản lý người dùng (nội bộ)")
public class InternalUserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Danh sách người dùng")
    public ResponseEntity<PageResponse<UserInternalResponse>> getUsers(
            @ParameterObject @ModelAttribute UserFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(userService.getUsers(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
    @Operation(summary = "Chi tiết người dùng")
    public ResponseEntity<SuccessResponse<UserInternalResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(userService.getUserById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo người dùng mới")
    public ResponseEntity<SuccessResponse<UserInternalResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserInternalResponse response = userService.createUser(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.user")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật người dùng")
    public ResponseEntity<SuccessResponse<UserInternalResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserInternalResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.user")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa người dùng")
    public ResponseEntity<SuccessResponse<Void>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails.getId().equals(id)) {
            throw SecurityCustomException.forbidden();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.user"))
        ));
    }
}
