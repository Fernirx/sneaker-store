package com.fernirx.sneakerapi.notification.event;

public record LowStockEvent(Long variantId, String productName, String sku, int newStock, int minStockLevel) {}
