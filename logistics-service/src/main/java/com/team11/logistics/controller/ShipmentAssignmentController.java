package com.team11.logistics.controller;

import com.team11.logistics.dto.VehicleAssignmentRequest;
import com.team11.logistics.entity.Shipment;
import com.team11.logistics.service.ShipmentAssignmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipments")
@Validated
public class ShipmentAssignmentController {

    private final ShipmentAssignmentService assignmentService;

    public ShipmentAssignmentController(
            ShipmentAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PutMapping("/{shipmentId}/vehicle")
    public ResponseEntity<Shipment> assignVehicle(
            @PathVariable @Positive Long shipmentId,
            @Valid @RequestBody VehicleAssignmentRequest request) {

        Shipment updated = assignmentService.assignVehicle(
                shipmentId, request.getVehicleId());

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{shipmentId}/vehicle")
    public ResponseEntity<Void> unassignVehicle(
            @PathVariable @Positive Long shipmentId) {

        assignmentService.unassignVehicle(shipmentId);

        return ResponseEntity.noContent().build();
    }
}
