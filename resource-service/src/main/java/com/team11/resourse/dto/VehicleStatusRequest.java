
package com.team11.resourse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VehicleStatusRequest(

    @NotBlank(message = "Status is required")
    @Pattern(
        regexp = "^(AVAILABLE|RESERVED|ASSIGNED|IN_USE|UNDER_MAINTENANCE|UNAVAILABLE)$",
        message = "Invalid vehicle status"
    )
    String status

) {}
