package com.fernirx.sneakerapi.auth.service.impl;

import com.fernirx.sneakerapi.auth.dto.request.*;
import com.fernirx.sneakerapi.auth.mapper.AuthMapper;
import com.fernirx.sneakerapi.auth.service.AuthService;
import com.fernirx.sneakerapi.auth.service.OtpService;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.customer.service.CustomerService;
import com.fernirx.sneakerapi.security.jwt.JwtProvider;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import com.fernirx.sneakerapi.security.model.UserTokenPayload;
import com.fernirx.sneakerapi.security.response.TokenResponse;
import com.fernirx.sneakerapi.security.service.TokenBlacklistService;
import com.fernirx.sneakerapi.user.dto.command.RegisterCommand;
import com.fernirx.sneakerapi.user.entity.User;
import com.fernirx.sneakerapi.user.enums.OtpPurpose;
import com.fernirx.sneakerapi.user.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserDetailsService userDetailsService;
    private final UserAccountService userAccountService;
    private final CustomerService customerService;
    private final AuthMapper authMapper;
    private final OtpService otpService;

    /**
     * Xác thực thông tin đăng nhập và trả về JWT Token.
     * Luồng xử lý:
     * 1. Gọi AuthenticationManager để xác thực email/password.
     * 2. Bắt và map các ngoại lệ bảo mật của Spring (Sai pass, khóa tài khoản, 
     *    chưa kích hoạt email) thành SecurityCustomException để trả về HTTP 401/403.
     * 3. Trích xuất thông tin UserDetails và tạo cặp Access Token / Refresh Token.
     */
    @Override
    public TokenResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw SecurityCustomException.invalidCredentials();
        } catch (LockedException e) {
            throw SecurityCustomException.accountUnavailable();
        } catch (DisabledException e) {
            throw SecurityCustomException.emailNotVerified();
        }

        UserTokenPayload payload = UserTokenPayload.from((CustomUserDetails) authentication.getPrincipal());
        return TokenResponse.builder()
                .accessToken(jwtProvider.generateAccessToken(payload))
                .refreshToken(jwtProvider.generateRefreshToken(payload))
                .build();
    }

    /**
     * Cấp lại Access Token mới dựa trên Refresh Token.
     * Luồng xử lý:
     * 1. Xác thực tính hợp lệ của Refresh Token (còn hạn, đúng chữ ký).
     * 2. Kiểm tra Refresh Token có nằm trong Blacklist hay không (cho phép 
     *    grace period 30s để xử lý các request đồng thời).
     * 3. Lấy thông tin user mới nhất từ DB để đảm bảo token mới cập nhật đúng quyền.
     * 4. Sinh cặp token mới và cho Refresh Token cũ vào Blacklist.
     */
    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String oldRefreshToken = request.refreshToken();
        jwtProvider.validateRefreshToken(oldRefreshToken);
        
        if (tokenBlacklistService.isRefreshTokenBlacklistedWithGracePeriod(oldRefreshToken, 30_000)) {
            throw SecurityCustomException.invalid("label.token");
        }
        
        String email = jwtProvider.extractEmail(oldRefreshToken);
        CustomUserDetails userDetails;
        try {
            userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            throw SecurityCustomException.invalid("label.token");
        }
        
        UserTokenPayload payload = UserTokenPayload.from(userDetails);
        String accessToken = jwtProvider.generateAccessToken(payload);
        String newRefreshToken = jwtProvider.generateRefreshToken(payload);
        
        tokenBlacklistService.blacklistRefreshToken(oldRefreshToken);
        
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    /**
     * Đăng ký tài khoản người dùng mới.
     * Luồng xử lý:
     * 1. Tạo tài khoản User (chưa kích hoạt).
     * 2. Khởi tạo profile Customer tương ứng.
     * 3. Gọi OtpService để gửi email chứa mã OTP kích hoạt tài khoản.
     */
    @Override
    public void register(RegisterRequest request) {
        RegisterCommand command = authMapper.toCommand(request);
        User user = userAccountService.createUserWithPassword(command);
        customerService.initCustomer(user);
        otpService.sendOtp(user.getEmail(), request.firstName(), OtpPurpose.REGISTER);
    }

    /**
     * Xác thực mã OTP để kích hoạt tài khoản vừa đăng ký.
     * Luồng xử lý:
     * 1. Kiểm tra tài khoản đã kích hoạt chưa. Nếu rồi thì ném lỗi.
     * 2. Gọi OtpService để xác minh tính hợp lệ của mã OTP.
     * 3. Cập nhật trạng thái email đã verify trong DB.
     * 4. Tự động cấp luôn JWT Token để người dùng đăng nhập ngay.
     */
    @Override
    public TokenResponse verifyOtp(VerifyOtpRequest request) {
        CustomUserDetails userDetails = loadUserOrThrow(request.email());
        if (userDetails.isEnabled()) {
            throw BusinessException.bad("label.user"); // Đã kích hoạt rồi
        }
        
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.REGISTER);
        userAccountService.verifyEmail(request.email());
        
        // Reload lại userDetails sau khi đã verify để lấy state mới nhất
        userDetails = loadUserOrThrow(request.email());
        UserTokenPayload payload = UserTokenPayload.from(userDetails);
        
        return TokenResponse.builder()
                .accessToken(jwtProvider.generateAccessToken(payload))
                .refreshToken(jwtProvider.generateRefreshToken(payload))
                .build();
    }

    /**
     * Đăng xuất người dùng.
     * Luồng xử lý:
     * Đưa cả Access Token và Refresh Token vào danh sách đen (Blacklist) trên 
     * Redis để vô hiệu hóa ngay lập tức.
     */
    @Override
    public void logout(LogoutRequest request) {
        jwtProvider.validateRefreshToken(request.refreshToken());
        tokenBlacklistService.blacklistRefreshToken(request.refreshToken());
        tokenBlacklistService.blacklistAccessToken(request.accessToken());
    }

    /**
     * Yêu cầu cấp lại mật khẩu (Quên mật khẩu).
     * Luồng xử lý:
     * 1. Kiểm tra email có tồn tại không.
     * 2. Nếu tài khoản chưa kích hoạt (chưa verify email), từ chối cho đổi mật khẩu.
     * 3. Gửi OTP cấp lại mật khẩu qua email.
     */
    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        CustomUserDetails userDetails = loadUserOrThrow(request.email());
        if (!userDetails.isEnabled()) {
            throw SecurityCustomException.emailNotVerified();
        }
        otpService.sendOtp(request.email(), request.email().split("@")[0], OtpPurpose.FORGOT_PASSWORD);
    }

    /**
     * Xác minh OTP cho luồng quên mật khẩu.
     * Luồng xử lý:
     * 1. Xác minh OTP từ request.
     * 2. Nếu hợp lệ, cấp một 'Reset Password Token' (JWT ngắn hạn với quyền hạn 
     *    đặc biệt) để qua bước đặt lại mật khẩu.
     */
    @Override
    public TokenResponse forgotPasswordVerifyOtp(VerifyOtpRequest request) {
        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.FORGOT_PASSWORD);
        CustomUserDetails userDetails = loadUserOrThrow(request.email());
        UserTokenPayload payload = UserTokenPayload.from(userDetails);
        return TokenResponse.builder()
                .resetPasswordToken(jwtProvider.generateResetPasswordToken(payload))
                .build();
    }

    /**
     * Đặt lại mật khẩu mới.
     * Luồng xử lý:
     * 1. Xác thực tính hợp lệ của 'Reset Password Token'.
     * 2. Cập nhật mật khẩu mới cho user lấy từ Token.
     */
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        jwtProvider.validateResetPasswordToken(request.resetToken());
        String email = jwtProvider.extractEmail(request.resetToken());
        userAccountService.updatePassword(email, request.password());
    }

    /**
     * Gửi lại mã OTP qua email (Dùng chung cho Đăng ký & Quên mật khẩu).
     * Điều kiện kiểm tra:
     * - Nếu là REGISTER: Phải đảm bảo tài khoản CHƯA kích hoạt. 
     *   (Chặn spam OTP kích hoạt nếu đã verify).
     * - Nếu là FORGOT_PASSWORD: Phải đảm bảo tài khoản ĐÃ kích hoạt. 
     *   (Chặn lấy lại mật khẩu của tài khoản ảo).
     */
    @Override
    public void resendOtp(ResendOtpRequest request) {
        CustomUserDetails userDetails = loadUserOrThrow(request.email());
        
        if (request.purpose() == OtpPurpose.REGISTER && userDetails.isEnabled()) {
            throw BusinessException.bad("label.user");
        }
        if (request.purpose() == OtpPurpose.FORGOT_PASSWORD && !userDetails.isEnabled()) {
            throw SecurityCustomException.emailNotVerified();
        }
        
        otpService.sendOtp(request.email(), request.email().split("@")[0], request.purpose());
    }

    /**
     * Hàm phụ trợ lấy thông tin CustomUserDetails từ DB theo email, 
     * bọc ngoại lệ ném ra lỗi BusinessException 404.
     */
    private CustomUserDetails loadUserOrThrow(String email) {
        try {
            return (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            throw BusinessException.notFound("label.email");
        }
    }
}