package com.fernirx.sneakerapi.shipping.dto.response;

import java.time.LocalDateTime;

public record ShipmentResult(
        String shippingOrderCode,
        LocalDateTime expectedDeliveryAt
) {}
