package com.team11.resourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record MaintenanceUpdateRequest(
        @NotBlank(message = "Resource type is required") @Size(max = 30) String resourceType,
        @NotNull(message = "Resource ID is required") @Positive(message = "Resource ID must be greater than zero") Long resourceId,
        @NotBlank(message = "Maintenance type is required") @Size(max = 50) String maintenanceType,
        @NotBlank(message = "Description is required") @Size(max = 1000) String description,
        @NotNull(message = "Scheduled date is required") LocalDateTime scheduledDate,
        LocalDateTime startDate,
        LocalDateTime completionDate,
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "^(SCHEDULED|IN_PROGRESS|COMPLETED|CANCELLED)$", message = "Invalid maintenance status")
        String status,
        @Size(max = 2000) String serviceNotes,
        @Size(max = 100) String technician
) {
}