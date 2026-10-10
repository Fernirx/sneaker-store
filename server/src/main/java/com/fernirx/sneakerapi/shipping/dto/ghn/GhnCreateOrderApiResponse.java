package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhnCreateOrderApiResponse(
        Integer code,
        String message,
        GhnCreateOrderResponse data,
        @JsonProperty("code_message") String codeMessage
) {}
