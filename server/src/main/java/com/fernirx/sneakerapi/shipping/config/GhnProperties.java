package com.fernirx.sneakerapi.shipping.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "ghn")
public class GhnProperties {
    /** URL API Master Data GHN v3 */
    @NotBlank
    private String apiUrl;

    /** Token xác thực API GHN */
    @NotBlank
    private String token;
}
