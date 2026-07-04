package com.fernirx.sneakerapi.shipping.dto.request;

import com.fernirx.sneakerapi.shipping.dto.ParcelItem;

import java.util.List;

public record PreviewOrderFeeRequest(
        String recipientName,
        String recipientPhone,
        String shippingStreet,
        String shippingWard,
        String shippingDistrict,
        String shippingProvince,
        List<ParcelItem> items
) {}
