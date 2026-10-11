package com.team11.resourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AssetCreateRequest(
        @NotBlank(message = "Asset tag is required")
        @Size(max = 50, message = "Asset tag must not exceed 50 characters")
        String assetTag,

        @NotBlank(message = "Asset name is required")
        @Size(max = 100, message = "Asset name must not exceed 100 characters")
        String assetName,

        @NotBlank(message = "Category is required")
        @Size(max = 50, message = "Category must not exceed 50 characters")
        String category,

        @NotNull(message = "Unit ID is required")
        @Positive(message = "Unit ID must be greater than zero")
        Long unitId,

        @NotBlank(message = "Location is required")
        @Size(max = 100, message = "Location must not exceed 100 characters")
        String location,

        @NotBlank(message = "Condition is required")
        @Size(max = 30, message = "Condition must not exceed 30 characters")
        String condition,

        @NotBlank(message = "Status is required")
        @Size(max = 30, message = "Status must not exceed 30 characters")
        String status
) {
}