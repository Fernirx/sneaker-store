package com.fernirx.sneakerapi.supplier.dto.request;

import com.fernirx.sneakerapi.supplier.enums.PurchasePaymentStatus;
import com.fernirx.sneakerapi.supplier.enums.PurchaseStatus;

import java.time.LocalDateTime;

public record PurchaseFilterRequest(
        String search,
        Long supplierId,
        PurchaseStatus status,
        PurchasePaymentStatus paymentStatus,
        LocalDateTime fromDate,
        LocalDateTime toDate
) {}
