package com.fernirx.sneakerapi.notification.event;

public record OrderCancelledEvent(Long orderId, String orderCode, String reason) {}
