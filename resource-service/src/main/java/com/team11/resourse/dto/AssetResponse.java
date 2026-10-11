package com.team11.resourse.dto;

import java.time.LocalDateTime;

public record AssetResponse(
        Long assetId,
        String assetTag,
        String assetName,
        String category,
        Long unitId,
        String location,
        String condition,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}