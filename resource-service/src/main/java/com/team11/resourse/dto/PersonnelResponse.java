package com.team11.resourse.dto;

import java.time.LocalDateTime;

public record PersonnelResponse(
        Long personnelId,
        String personnelCode,
        String fullName,
        String role,
        Long unitId,
        String postingLocation,
        String availability,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}