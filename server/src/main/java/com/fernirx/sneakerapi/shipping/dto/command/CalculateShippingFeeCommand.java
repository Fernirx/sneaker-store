package com.fernirx.sneakerapi.shipping.dto.command;

import com.fernirx.sneakerapi.shipping.dto.ParcelItem;

import java.math.BigDecimal;
import java.util.List;

public record CalculateShippingFeeCommand(
        String recipientName,
        String recipientPhone,
        String shippingStreet,
        String shippingWard,
        String shippingDistrict,
        String shippingProvince,
        BigDecimal subtotal,
        List<ParcelItem> items
) {}
