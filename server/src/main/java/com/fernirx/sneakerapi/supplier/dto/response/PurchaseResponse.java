package com.fernirx.sneakerapi.supplier.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fernirx.sneakerapi.supplier.enums.PurchasePaymentStatus;
import com.fernirx.sneakerapi.supplier.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PurchaseResponse(
        Long id,
        Long supplierId,
        String supplierName,
        String purchaseCode,
        String supplierInvoiceNo,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal shippingCost,
        BigDecimal totalCost,
        PurchasePaymentStatus paymentStatus,
        PurchaseStatus status,
        String notes,
        Long createdById,
        String createdByEmail,
        Long receivedById,
        String receivedByEmail,
        LocalDateTime confirmedAt,
        LocalDateTime receivedAt,
        LocalDateTime createdAt,
        List<PurchaseItemResponse> items
) {}
