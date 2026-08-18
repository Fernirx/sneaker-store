package com.fernirx.sneakerapi.auth.service.impl;

import com.fernirx.sneakerapi.auth.mapper.AuthMapper;
import com.fernirx.sneakerapi.common.enums.Role;
import com.fernirx.sneakerapi.customer.service.CustomerService;
import com.fernirx.sneakerapi.security.mapper.UserSecurityMapper;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.security.model.UserTokenPayload;
import com.fernirx.sneakerapi.security.oauth2.OAuth2UserInfo;
import com.fernirx.sneakerapi.security.oauth2.OAuth2UserProcessor;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2UserProcessorImpl implements OAuth2UserProcessor {
    private final UserAccountService userAccountService;
    private final CustomerService customerService;
    private final UserSecurityMapper userSecurityMapper;
    private final AuthMapper authMapper;

    /**
     * Xử lý luồng đăng nhập qua mạng xã hội (Google, Facebook...).
     * Luồng xử lý:
     * 1. Nhận thông tin OAuth2 (email, tên, avatar) từ nhà cung cấp dịch vụ.
     * 2. Gọi UserAccountService để tìm tài khoản có email tương ứng. 
     *    Nếu chưa có thì tự động tạo mới (mặc định đã kích hoạt).
     * 3. Khởi tạo profile Customer tương ứng cho User (nếu chưa có).
     * 4. Bọc thông tin user vào CustomUserDetails và sinh Payload để hệ thống 
     *    JWT cấp token ở bước tiếp theo.
     */
    @Override
    public UserTokenPayload process(OAuth2UserInfo userInfo) {
        User user = userAccountService.findOrCreateOAuth2User(authMapper.toCommand(userInfo));
        boolean isCustomer = user.getUserRoles().stream()
                .anyMatch(r -> r.getRole() == Role.ROLE_CUSTOMER);
        if (isCustomer) {
            customerService.initCustomer(user);
        }
        CustomUserDetails userDetails = userSecurityMapper.toCustomUserDetails(user);
        return UserTokenPayload.from(userDetails);
    }
}