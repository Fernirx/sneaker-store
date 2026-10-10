package com.fernirx.sneakerapi.notification.event;

public record ProductOnSaleEvent(Long productId, String productName, String slug) {}
