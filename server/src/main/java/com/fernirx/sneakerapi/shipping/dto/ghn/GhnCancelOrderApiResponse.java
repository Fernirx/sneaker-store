package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhnCancelOrderApiResponse(
        Integer code,
        String message,
        List<GhnCancelOrderResult> data,
        @JsonProperty("code_message") String codeMessage
) {}
