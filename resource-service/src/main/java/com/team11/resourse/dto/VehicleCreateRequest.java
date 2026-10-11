
package com.team11.resourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VehicleCreateRequest(

    @NotBlank(message = "Vehicle number is required")
    @Size(max = 30, message = "Vehicle number must not exceed 30 characters")
    String vehicleNumber,

    @NotBlank(message = "Vehicle type is required")
    @Size(max = 50, message = "Vehicle type must not exceed 50 characters")
    String vehicleType,

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than zero")
    Integer capacity,

    Long unitId,

    @Size(max = 50, message = "Location code must not exceed 50 characters")
    String locationCode,

    @NotBlank(message = "Condition is required")
    @Size(max = 30, message = "Condition must not exceed 30 characters")
    String condition,

    @NotBlank(message = "Status is required")
    @Pattern(
        regexp = "^(AVAILABLE|RESERVED|ASSIGNED|IN_USE|UNDER_MAINTENANCE|UNAVAILABLE)$",
        message = "Invalid vehicle status"
    )
    String status

) {}
