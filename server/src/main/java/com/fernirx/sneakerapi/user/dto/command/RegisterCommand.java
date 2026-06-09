package com.fernirx.sneakerapi.user.dto.command;

public record RegisterCommand(
        String email,
        String password,
        String firstName,
        String lastName
) {}
