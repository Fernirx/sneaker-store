package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GhnOrderDetailApiResponse(
        Integer code,
        String message,
        GhnOrderDetailItem data
) {}
