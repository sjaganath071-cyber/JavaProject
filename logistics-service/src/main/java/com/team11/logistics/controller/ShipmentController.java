package com.team11.logistics.controller;

import com.team11.logistics.entity.Shipment;
import com.team11.logistics.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping
    public ResponseEntity<Shipment> createShipment(
            @Valid @RequestBody Shipment shipment) {

        Shipment created = shipmentService.createShipment(shipment);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Shipment>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                shipmentService.getShipmentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Shipment> updateShipment(
            @PathVariable Long id,
            @Valid @RequestBody Shipment shipment) {

        return ResponseEntity.ok(
                shipmentService.updateShipment(id, shipment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(
            @PathVariable Long id) {

        shipmentService.deleteShipment(id);

        return ResponseEntity.noContent().build();
    }
}
