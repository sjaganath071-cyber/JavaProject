package com.team11.resourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PersonnelAvailabilityRequest(
        @NotBlank(message = "Availability is required")
        @Size(max = 30, message = "Availability must not exceed 30 characters")
        String availability
) {
}