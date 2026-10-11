package com.team11.logistics.repository;

import com.team11.logistics.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    boolean existsByShipmentNumber(String shipmentNumber);

    boolean existsByShipmentNumberAndIdNot(
            String shipmentNumber, Long id);
}
