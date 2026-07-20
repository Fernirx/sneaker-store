package com.fernirx.sneakerapi.notification.event;

public record ReturnRequestCompletedEvent(Long returnRequestId, String code, Long customerUserId) {}
