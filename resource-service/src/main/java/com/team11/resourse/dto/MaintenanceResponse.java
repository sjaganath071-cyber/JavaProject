package com.team11.resourse.dto;

import java.time.LocalDateTime;

public record MaintenanceResponse(
        Long maintenanceId,
        String resourceType,
        Long resourceId,
        String maintenanceType,
        String description,
        LocalDateTime scheduledDate,
        LocalDateTime startDate,
        LocalDateTime completionDate,
        String status,
        String serviceNotes,
        String technician,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}