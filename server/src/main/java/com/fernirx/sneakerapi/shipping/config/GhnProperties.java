package com.fernirx.sneakerapi.shipping.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "ghn")
public class GhnProperties {
    /** URL gốc dùng chung cho mọi API GHN (master-data, shipping-order) - phần path còn lại cố định trong code */
    @NotBlank
    private String url = "https://dev-online-gateway.ghn.vn/shiip/public-api";

    /** Token xác thực API GHN */
    @NotBlank
    private String token;

    /** Shop ID GHN */
    private Integer shopId;

    /** Phí vận chuyển mặc định dự phòng khi API GHN lỗi */
    private java.math.BigDecimal fallbackFee = java.math.BigDecimal.valueOf(30000);
}
