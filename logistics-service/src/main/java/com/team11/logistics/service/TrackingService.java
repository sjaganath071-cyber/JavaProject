package com.team11.logistics.service;

import com.team11.logistics.dto.TrackingRequest;
import com.team11.logistics.entity.ShipmentTracking;
import com.team11.logistics.repository.ShipmentRepository;
import com.team11.logistics.repository.TrackingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrackingService {

    private final TrackingRepository trackingRepository;
    private final ShipmentRepository shipmentRepository;

    public TrackingService(
            TrackingRepository trackingRepository,
            ShipmentRepository shipmentRepository) {
        this.trackingRepository = trackingRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public ShipmentTracking createTracking(
            Long shipmentId, TrackingRequest request) {

        shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shipment not found with ID: " + shipmentId));

        ShipmentTracking tracking = new ShipmentTracking();
        tracking.setShipmentId(shipmentId);
        tracking.setLocation(request.getLocation().trim());
        tracking.setStatus(request.getStatus().trim());

        if (request.getRemarks() != null) {
            tracking.setRemarks(request.getRemarks().trim());
        }

        return trackingRepository.save(tracking);
    }

    @Transactional(readOnly = true)
    public List<ShipmentTracking> getTrackingHistory(Long shipmentId) {

        shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shipment not found with ID: " + shipmentId));

        return trackingRepository
                .findByShipmentIdOrderByUpdatedAtDesc(shipmentId);
    }
}
