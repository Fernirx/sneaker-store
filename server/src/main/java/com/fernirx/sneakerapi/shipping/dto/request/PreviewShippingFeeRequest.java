package com.fernirx.sneakerapi.shipping.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PreviewShippingFeeRequest(
        @NotBlank String shippingStreet,
        @NotBlank String shippingWard,
        @NotBlank String shippingDistrict,
        @NotBlank String shippingProvince,
        @NotEmpty @Valid List<ShippingItemRequest> items
) {}
