package com.bidyatra.staff.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ActivityRequest(
        @NotNull LocalDate date,
        @NotBlank String staffName,
        String visitingLocation,
        @Min(0) int meetDriver,
        @Min(0) int login,
        @Min(0) int documentsIssues,
        String other
) {}
