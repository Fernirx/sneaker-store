package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhnCreateOrderResponse(
        @JsonProperty("order_code") String orderCode,
        @JsonProperty("expected_delivery_time") Instant expectedDeliveryTime
) {}
