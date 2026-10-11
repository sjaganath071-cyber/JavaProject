package com.team11.resourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PersonnelUpdateRequest(
        @NotBlank(message = "Personnel code is required")
        @Size(max = 30, message = "Personnel code must not exceed 30 characters")
        String personnelCode,

        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        String fullName,

        @NotBlank(message = "Role is required")
        @Size(max = 50, message = "Role must not exceed 50 characters")
        String role,

        @NotNull(message = "Unit ID is required")
        @Positive(message = "Unit ID must be greater than zero")
        Long unitId,

        @Size(max = 100, message = "Posting location must not exceed 100 characters")
        String postingLocation,

        @Size(max = 30, message = "Availability must not exceed 30 characters")
        String availability,

        Boolean active
) {
}