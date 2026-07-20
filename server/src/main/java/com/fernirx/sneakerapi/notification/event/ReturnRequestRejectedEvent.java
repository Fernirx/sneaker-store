package com.fernirx.sneakerapi.notification.event;

public record ReturnRequestRejectedEvent(Long returnRequestId, String code, String reason, Long customerUserId) {}
