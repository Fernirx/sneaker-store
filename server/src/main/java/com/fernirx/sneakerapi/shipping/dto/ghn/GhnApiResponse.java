package com.fernirx.sneakerapi.shipping.dto.ghn;

import java.util.List;

public record GhnApiResponse<T>(
        Integer code,
        String message,
        List<T> data
) {}
