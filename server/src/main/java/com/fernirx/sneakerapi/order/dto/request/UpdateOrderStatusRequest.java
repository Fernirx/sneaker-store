package com.fernirx.sneakerapi.order.dto.request;

import com.fernirx.sneakerapi.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "{validation.field.not_blank}")
        OrderStatus status,

        String note
) {}
