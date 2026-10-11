package com.team11.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VehicleRequest {

    @NotBlank(message = "Vehicle name is required")
    @Size(max = 100, message = "Vehicle name must not exceed 100 characters")
    private String vehicleName;

    @NotBlank(message = "Vehicle type is required")
    @Size(max = 50, message = "Vehicle type must not exceed 50 characters")
    private String vehicleType;

    @NotBlank(message = "Registration number is required")
    @Size(max = 30, message = "Registration number must not exceed 30 characters")
    private String registrationNumber;

    @NotBlank(message = "Vehicle status is required")
    @Size(max = 30, message = "Vehicle status must not exceed 30 characters")
    private String status;

    public VehicleRequest() {
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
