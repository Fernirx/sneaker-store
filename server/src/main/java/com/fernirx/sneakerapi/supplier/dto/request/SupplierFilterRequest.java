package com.fernirx.sneakerapi.supplier.dto.request;

public record SupplierFilterRequest(
        String search,
        Boolean active
) {}
