package com.team11.defense_logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VehicleDto {

    private Long id;

    @NotBlank(message = "Vehicle identification number is required")
    @Size(max = 50, message = "Vehicle identification number must not exceed 50 characters")
    private String vehicleCode;

    @NotBlank(message = "Vehicle name is required")
    @Size(max = 100, message = "Vehicle name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Vehicle type is required")
    @Size(max = 100, message = "Vehicle type must not exceed 100 characters")
    private String type;

    @NotBlank(message = "Assigned unit is required")
    @Size(max = 100, message = "Assigned unit must not exceed 100 characters")
    private String unit;

    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @NotBlank(message = "Vehicle condition is required")
    private String condition;

    @NotBlank(message = "Vehicle status is required")
    private String status;

    public VehicleDto() {
    }

    public VehicleDto(
            Long id,
            String vehicleCode,
            String name,
            String type,
            String unit,
            String location,
            String condition,
            String status) {

        this.id = id;
        this.vehicleCode = vehicleCode;
        this.name = name;
        this.type = type;
        this.unit = unit;
        this.location = location;
        this.condition = condition;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
