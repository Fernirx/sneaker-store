package com.fernirx.sneakerapi.product.dto.request;

public record UpdateImageRequest(
        Boolean primaryImage,
        Integer displayOrder
) {}
