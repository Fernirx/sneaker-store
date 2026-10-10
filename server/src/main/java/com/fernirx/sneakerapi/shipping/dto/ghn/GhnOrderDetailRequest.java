package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GhnOrderDetailRequest(
        @JsonProperty("client_order_code") String clientOrderCode
) {}
