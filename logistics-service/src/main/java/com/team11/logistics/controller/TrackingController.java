package com.team11.logistics.controller;

import com.team11.logistics.dto.TrackingRequest;
import com.team11.logistics.entity.ShipmentTracking;
import com.team11.logistics.service.TrackingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipments/{shipmentId}/tracking")
@Validated
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping
    public ResponseEntity<ShipmentTracking> createTracking(
            @PathVariable @Positive Long shipmentId,
            @Valid @RequestBody TrackingRequest request) {

        ShipmentTracking created =
                trackingService.createTracking(shipmentId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ShipmentTracking>> getTrackingHistory(
            @PathVariable @Positive Long shipmentId) {

        return ResponseEntity.ok(
                trackingService.getTrackingHistory(shipmentId));
    }
}
