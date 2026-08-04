package com.fernirx.sneakerapi.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateColorwayGroupRequest(
        @NotBlank
        @Size(max = 100)
        String oldColorway,

        @NotBlank
        @Size(max = 100)
        String newColorway,
        
        @Size(max = 50)
        String newColorwayCode,
        
        @Size(max = 7)
        String newColorHex
) {
}
