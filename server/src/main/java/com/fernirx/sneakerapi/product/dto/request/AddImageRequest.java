package com.fernirx.sneakerapi.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddImageRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String colorway,

        @Size(max = 7, message = "{validation.size.max}")
        String colorHex,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        String imagePublicId,

        Boolean primaryImage,

        Integer displayOrder
) {}
