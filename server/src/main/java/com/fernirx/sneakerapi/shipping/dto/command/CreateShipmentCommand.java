package com.fernirx.sneakerapi.shipping.dto.command;

import com.fernirx.sneakerapi.shipping.dto.ParcelItem;

import java.util.List;

public record CreateShipmentCommand(
        String recipientName,
        String recipientPhone,
        String shippingStreet,
        String shippingWard,
        String shippingDistrict,
        String shippingProvince,
        String clientOrderCode,
        Long codAmount,
        Integer paymentTypeId,
        String note,
        List<ParcelItem> items
) {}
