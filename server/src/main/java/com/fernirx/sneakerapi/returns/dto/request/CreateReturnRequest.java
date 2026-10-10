package com.fernirx.sneakerapi.returns.dto.request;

import com.fernirx.sneakerapi.returns.enums.ReturnResolutionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateReturnRequest(
        @NotNull(message = "{validation.field.not_blank}")
        Long orderId,

        @NotNull(message = "{validation.field.not_blank}")
        ReturnResolutionType resolutionType,

        @NotBlank(message = "{validation.field.not_blank}")
        String reason,

        @NotEmpty(message = "{validation.field.not_blank}")
        @Valid
        List<ReturnItemRequest> items,

        @Size(max = 5, message = "{validation.list.max}")
        List<@NotBlank(message = "{validation.field.not_blank}") String> imagePublicIds
) {}
