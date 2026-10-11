package com.team11.logistics.repository;

import com.team11.logistics.entity.Shipment;
import com.team11.logistics.model.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentSearchRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByStatus(ShipmentStatus status);

    List<Shipment> findByOriginContainingIgnoreCase(String origin);

    List<Shipment> findByDestinationContainingIgnoreCase(
            String destination);

    List<Shipment> findByStatusAndOriginContainingIgnoreCase(
            ShipmentStatus status, String origin);

    List<Shipment> findByStatusAndDestinationContainingIgnoreCase(
            ShipmentStatus status, String destination);
}
