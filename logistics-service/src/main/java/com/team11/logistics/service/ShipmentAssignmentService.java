package com.team11.logistics.service;

import com.team11.logistics.entity.Shipment;
import com.team11.logistics.repository.ShipmentRepository;
import com.team11.logistics.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShipmentAssignmentService {

    private final ShipmentRepository shipmentRepository;
    private final VehicleRepository vehicleRepository;

    public ShipmentAssignmentService(
            ShipmentRepository shipmentRepository,
            VehicleRepository vehicleRepository) {
        this.shipmentRepository = shipmentRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Shipment assignVehicle(Long shipmentId, Long vehicleId) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shipment not found with ID: " + shipmentId));

        vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Vehicle not found with ID: " + vehicleId));

        shipment.setVehicleId(vehicleId);

        return shipmentRepository.save(shipment);
    }

    @Transactional
    public void unassignVehicle(Long shipmentId) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shipment not found with ID: " + shipmentId));

        shipment.setVehicleId(null);

        shipmentRepository.save(shipment);
    }
}
