package com.team11.resourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaintenanceNotesRequest(
        @NotBlank(message = "Service notes are required")
        @Size(max = 2000, message = "Service notes must not exceed 2000 characters")
        String serviceNotes
) {
}