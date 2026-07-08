package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhnOrderDetailItem(
        @JsonProperty("client_order_code") String clientOrderCode,
        @JsonProperty("order_code") String orderCode,
        String status,
        Instant leadtime,
        @JsonProperty("finish_date") Instant finishDate
) {}
