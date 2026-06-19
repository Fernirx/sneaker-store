package com.fernirx.sneakerapi.supplier.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CreatePurchaseRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long supplierId,

        @NullableNotBlank
        @Size(max = 100, message = "{validation.size.max}")
        String supplierInvoiceNo,

        @DecimalMin(value = "0", message = "{validation.number.min}")
        BigDecimal discountAmount,

        @DecimalMin(value = "0", message = "{validation.number.min}")
        BigDecimal taxAmount,

        @DecimalMin(value = "0", message = "{validation.number.min}")
        BigDecimal shippingCost,

        @NullableNotBlank
        String notes,

        @NotEmpty(message = "{validation.field.not_blank}")
        @Valid
        List<PurchaseItemRequest> items
) {}
