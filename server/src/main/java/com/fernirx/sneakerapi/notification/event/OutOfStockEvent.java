package com.fernirx.sneakerapi.notification.event;

public record OutOfStockEvent(Long variantId, String productName, String sku) {}
