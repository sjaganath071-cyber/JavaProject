package com.team11.logistics.repository;

import com.team11.logistics.entity.ShipmentTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingRepository extends JpaRepository<ShipmentTracking, Long> {

    List<ShipmentTracking> findByShipmentIdOrderByUpdatedAtDesc(Long shipmentId);
}
