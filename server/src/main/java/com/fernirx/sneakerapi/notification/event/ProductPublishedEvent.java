package com.fernirx.sneakerapi.notification.event;

public record ProductPublishedEvent(Long productId, String productName, String slug) {}
