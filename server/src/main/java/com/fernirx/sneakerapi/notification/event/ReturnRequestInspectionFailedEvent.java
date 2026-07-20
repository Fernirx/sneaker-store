package com.fernirx.sneakerapi.notification.event;

public record ReturnRequestInspectionFailedEvent(Long returnRequestId, String code, String reason, Long customerUserId) {}
