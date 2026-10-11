
package com.team11.resourse.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

public record VehicleResponse(
    Long vehicleId,
    String vehicleNumber,
    String vehicleType,
    Integer capacity,
    Long unitId,
    String locationCode,
    String condition,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    @JsonIgnore
    public Long id() {
        return vehicleId;
    }
}
