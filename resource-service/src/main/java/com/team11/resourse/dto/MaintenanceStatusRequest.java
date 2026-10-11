package com.team11.resourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MaintenanceStatusRequest(
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "^(SCHEDULED|IN_PROGRESS|COMPLETED|CANCELLED)$", message = "Invalid maintenance status")
        String status
) {
}