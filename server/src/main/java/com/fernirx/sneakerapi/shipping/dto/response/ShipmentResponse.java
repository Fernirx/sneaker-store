package com.fernirx.sneakerapi.shipping.dto.response;

import java.time.LocalDateTime;

public record ShipmentResponse(
        String shippingOrderCode,
        String status,
        LocalDateTime expectedDeliveryAt,
        LocalDateTime syncedAt
) {}
