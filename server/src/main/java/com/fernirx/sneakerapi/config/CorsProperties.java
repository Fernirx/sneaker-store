package com.fernirx.sneakerapi.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "application.cors")
public class CorsProperties {
    /** Danh sách origin được phép gửi CORS request */
    @NotEmpty
    private List<String> allowedOrigins;

    /** Các HTTP method được phép sử dụng trong CORS request */
    @NotEmpty
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

    /** Các header được phép trong CORS request */
    @NotEmpty
    private List<String> allowedHeaders = List.of("*");

    /** Có cho phép gửi credentials (cookie, authorization header) trong CORS request hay không */
    private Boolean allowCredentials = true;

    /** Thời gian cache của CORS preflight request */
    private Duration maxAge = Duration.parse("PT1H");
}