package com.fernirx.sneakerapi.security.jwt;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Data
@Validated
@ConfigurationProperties(prefix = "application.security.jwt")
public class JwtProperties {
    /** Khóa bí mật dùng để ký JWT — bắt buộc set, không có default */
    @NotBlank
    private String secret;

    /** Đơn vị phát hành (issuer) ghi trong JWT claim */
    @NotBlank
    private String issuer = "sneaker-api";

    /** Cấu hình access token */
    private AccessToken access = new AccessToken();

    /** Cấu hình refresh token */
    private RefreshToken refresh = new RefreshToken();

    /** Cấu hình token đặt lại mật khẩu */
    private ResetPasswordToken resetPassword = new ResetPasswordToken();

    @Data
    public static class AccessToken {
        /** Thời gian hết hạn */
        private Duration expiration = Duration.parse("PT10M");
    }

    @Data
    public static class RefreshToken {
        /** Thời gian hết hạn */
        private Duration expiration = Duration.parse("P7D");
    }

    @Data
    public static class ResetPasswordToken {
        /** Thời gian hết hạn */
        private Duration expiration = Duration.parse("PT15M");
    }
}
