package com.fernirx.sneakerapi.customer.config;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Data
@Validated
@ConfigurationProperties(prefix = "customer")
public class CustomerProperties {
    /** Số tiền (VND) cần chi tiêu để được 1 điểm tích lũy */
    @Positive
    private BigDecimal pointsPerAmount = BigDecimal.valueOf(10000);

    /** Tổng chi tiêu (totalSpent) tối thiểu để lên hạng SILVER */
    @Positive
    private BigDecimal silverThreshold = BigDecimal.valueOf(5_000_000);

    /** Tổng chi tiêu (totalSpent) tối thiểu để lên hạng GOLD */
    @Positive
    private BigDecimal goldThreshold = BigDecimal.valueOf(15_000_000);

    /** Tổng chi tiêu (totalSpent) tối thiểu để lên hạng PLATINUM */
    @Positive
    private BigDecimal platinumThreshold = BigDecimal.valueOf(30_000_000);
}
