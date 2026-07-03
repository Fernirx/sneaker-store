package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;

public record GhnFeeResponse(
        @JsonAlias({"total", "Total"})
        BigDecimal total,
        @JsonAlias({"service_fee", "ServiceFee"})
        BigDecimal serviceFee
) {}
