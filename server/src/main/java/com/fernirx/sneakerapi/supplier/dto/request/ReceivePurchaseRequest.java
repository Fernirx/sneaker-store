package com.fernirx.sneakerapi.supplier.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReceivePurchaseRequest(
        @NotEmpty(message = "{validation.field.not_blank}")
        @Valid
        List<ReceivePurchaseItemRequest> items
) {}
