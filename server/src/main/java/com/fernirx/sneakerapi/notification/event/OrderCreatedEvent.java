package com.fernirx.sneakerapi.notification.event;

public record OrderCreatedEvent(Long orderId, String orderCode) {}
