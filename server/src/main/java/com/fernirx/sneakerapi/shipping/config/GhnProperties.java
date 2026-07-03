package com.fernirx.sneakerapi.shipping.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "ghn")
public class GhnProperties {
    /** URL API Master Data GHN (province/district/ward) */
    @NotBlank
    private String apiUrl;

    /** URL API tính phí ship GHN v2 */
    @NotBlank
    private String feeUrl = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";

    /** Token xác thực API GHN */
    @NotBlank
    private String token;

    /** Shop ID GHN */
    private Integer shopId;

    /** Mã Phường/Xã kho hàng gửi đi (from_ward_id_v2) */
    private Integer fromWardCode;

    /** Tên Phường/Xã kho hàng gửi đi */
    private String fromWardName;

    /** Địa chỉ đường kho hàng gửi đi (street) */
    private String fromStreet;

    /** Phí vận chuyển mặc định dự phòng khi API GHN lỗi */
    private java.math.BigDecimal fallbackFee = java.math.BigDecimal.valueOf(30000);
}
