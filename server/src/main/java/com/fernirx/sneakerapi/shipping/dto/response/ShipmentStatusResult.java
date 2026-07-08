package com.fernirx.sneakerapi.shipping.dto.response;

import java.time.LocalDateTime;

public record ShipmentStatusResult(
        String status,
        LocalDateTime expectedDeliveryAt,
        LocalDateTime deliveredAt
) {}
