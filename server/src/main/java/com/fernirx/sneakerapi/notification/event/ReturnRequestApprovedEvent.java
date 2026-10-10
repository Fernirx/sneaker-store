package com.fernirx.sneakerapi.notification.event;

public record ReturnRequestApprovedEvent(Long returnRequestId, String code, Long customerUserId) {}
