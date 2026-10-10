package com.fernirx.sneakerapi.supplier.dto.response;

import java.math.BigDecimal;

public record PurchaseItemResponse(
        Long id,
        Long variantId,
        String sku,
        String productName,
        Short size,
        String colorway,
        Integer quantityOrdered,
        Integer quantityReceived,
        Integer defectiveQty,
        BigDecimal unitCost,
        BigDecimal lineTotal,
        String notes
) {}
