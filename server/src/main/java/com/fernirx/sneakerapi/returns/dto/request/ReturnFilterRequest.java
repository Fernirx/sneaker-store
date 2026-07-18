package com.fernirx.sneakerapi.returns.dto.request;

import com.fernirx.sneakerapi.returns.enums.ReturnResolutionType;
import com.fernirx.sneakerapi.returns.enums.ReturnStatus;

import java.time.LocalDateTime;

public record ReturnFilterRequest(
        String search,
        ReturnResolutionType resolutionType,
        ReturnStatus status,
        LocalDateTime fromDate,
        LocalDateTime toDate
) {}
