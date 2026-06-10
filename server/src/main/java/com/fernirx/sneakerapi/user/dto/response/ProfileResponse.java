package com.fernirx.sneakerapi.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfileResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        LocalDate dateOfBirth,
        String avatarPublicId,
        boolean emailVerified,
        LocalDateTime createdAt
) {}