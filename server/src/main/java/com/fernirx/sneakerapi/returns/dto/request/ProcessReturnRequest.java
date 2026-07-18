package com.fernirx.sneakerapi.returns.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.NotNull;

public record ProcessReturnRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Boolean passed,

        @NullableNotBlank
        String rejectReason,

        @NullableNotBlank
        String adminNote
) {}
