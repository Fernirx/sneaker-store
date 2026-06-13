package com.fernirx.sneakerapi.product.dto.response;

import java.util.List;

public record ProductImageGroupResponse(
        String colorway,
        String colorHex,
        List<ImageResponse> images
) {
    public record ImageResponse(
            Long id,
            String publicId,
            Boolean primaryImage,
            Integer displayOrder
    ) {}
}
