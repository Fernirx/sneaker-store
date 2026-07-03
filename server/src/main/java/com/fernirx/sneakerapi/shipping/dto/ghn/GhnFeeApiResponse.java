package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GhnFeeApiResponse(
        Integer code,
        String message,
        GhnFeeResponse data,
        @JsonProperty("code_message") String codeMessage,
        @JsonProperty("code_message_value") String codeMessageValue
) {}
