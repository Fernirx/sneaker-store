package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhnPreviewApiResponse(
        Integer code,
        String message,
        GhnPreviewResponse data,
        @JsonProperty("code_message") String codeMessage
) {}
