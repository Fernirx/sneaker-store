package com.fernirx.sneakerapi.shipping.dto;

public record ParcelItem(
        Integer weight,
        Integer length,
        Integer width,
        Integer height,
        Integer quantity
) {}
