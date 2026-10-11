package com.team11.resourse.controller;

import com.team11.resourse.dto.VehicleCreateRequest;
import com.team11.resourse.dto.VehicleResponse;
import com.team11.resourse.service.VehicleService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse createVehicle(
            @Valid @RequestBody VehicleCreateRequest request) {
        return vehicleService.createVehicle(request);
    }

    @GetMapping
    public List<VehicleResponse> getAllVehicles(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) String locationCode,
            @RequestParam(required = false) String status) {
        return vehicleService.getAllVehicles(unitId, vehicleType, locationCode, status);
    }

    @GetMapping("/availability")
    public List<VehicleResponse> getAvailableVehicles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime until,
            @RequestParam(required = false) Long unitId) {
        return vehicleService.getAvailableVehicles(from, until, unitId);
    }

    @GetMapping("/{id}")
    public VehicleResponse getVehicleById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id);
    }
    @PatchMapping("/{id}/status")
public VehicleResponse updateVehicleStatus(
        @PathVariable Long id,
        @Valid @RequestBody com.team11.resourse.dto.VehicleStatusRequest request) {
    return vehicleService.updateVehicleStatus(id, request);
}
@PutMapping("/{id}")
public VehicleResponse updateVehicle(
        @PathVariable Long id,
        @Valid @RequestBody VehicleCreateRequest request) {
    return vehicleService.updateVehicle(id, request);
}
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void deleteVehicle(@PathVariable Long id) {
    vehicleService.deleteVehicle(id);
}
}