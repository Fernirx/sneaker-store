package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhnPreviewResponse(
        @JsonProperty("total_fee") BigDecimal totalFee,
        @JsonProperty("expected_delivery_time") Instant expectedDeliveryTime
) {}
