package com.team11.logistics.service;

import com.team11.logistics.entity.Shipment;
import com.team11.logistics.model.ShipmentStatus;
import com.team11.logistics.repository.ShipmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public Shipment createShipment(Shipment shipment) {
        shipment.setId(null);
        normalize(shipment);

        if (shipmentRepository.existsByShipmentNumber(
                shipment.getShipmentNumber())) {
            throw new IllegalStateException(
                    "Shipment number already exists");
        }

        if (shipment.getStatus() == null) {
            shipment.setStatus(ShipmentStatus.PLANNED);
        }

        return shipmentRepository.save(shipment);
    }

    @Transactional(readOnly = true)
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Shipment getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Shipment not found with ID: " + id));
    }

    @Transactional
    public Shipment updateShipment(Long id, Shipment request) {
        Shipment existing = getShipmentById(id);
        normalize(request);

        if (shipmentRepository.existsByShipmentNumberAndIdNot(
                request.getShipmentNumber(), id)) {
            throw new IllegalStateException(
                    "Another shipment uses this shipment number");
        }

        existing.setShipmentNumber(request.getShipmentNumber());
        existing.setOrigin(request.getOrigin());
        existing.setDestination(request.getDestination());
        existing.setCargoDescription(request.getCargoDescription());
        existing.setStatus(request.getStatus());
        existing.setVehicleId(request.getVehicleId());

        return shipmentRepository.save(existing);
    }

    @Transactional
    public void deleteShipment(Long id) {
        Shipment shipment = getShipmentById(id);
        shipmentRepository.delete(shipment);
    }

    private void normalize(Shipment shipment) {
        if (shipment.getShipmentNumber() != null) {
            shipment.setShipmentNumber(
                    shipment.getShipmentNumber().trim());
        }

        if (shipment.getOrigin() != null) {
            shipment.setOrigin(shipment.getOrigin().trim());
        }

        if (shipment.getDestination() != null) {
            shipment.setDestination(shipment.getDestination().trim());
        }

        if (shipment.getCargoDescription() != null) {
            shipment.setCargoDescription(
                    shipment.getCargoDescription().trim());
        }
    }
}
