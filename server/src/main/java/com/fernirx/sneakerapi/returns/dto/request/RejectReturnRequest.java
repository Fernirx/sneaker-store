package com.fernirx.sneakerapi.returns.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectReturnRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        String rejectReason
) {}
