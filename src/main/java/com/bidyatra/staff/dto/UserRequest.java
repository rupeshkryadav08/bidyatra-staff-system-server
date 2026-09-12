package com.bidyatra.staff.dto;

import com.bidyatra.staff.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotBlank String name,
        @NotBlank String username,
        @NotBlank String password,
        @NotNull Role role
) {}
