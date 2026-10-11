package com.team11.logistics.entity;

import com.team11.logistics.model.ShipmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "LOGISTICS_SHIPMENTS")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Shipment number is required")
    @Size(max = 40)
    @Column(name = "SHIPMENT_NUMBER", nullable = false, unique = true, length = 40)
    private String shipmentNumber;

    @NotBlank(message = "Origin is required")
    @Size(max = 150)
    @Column(name = "ORIGIN", nullable = false, length = 150)
    private String origin;

    @NotBlank(message = "Destination is required")
    @Size(max = 150)
    @Column(name = "DESTINATION", nullable = false, length = 150)
    private String destination;

    @Size(max = 500)
    @Column(name = "CARGO_DESCRIPTION", length = 500)
    private String cargoDescription;

    @NotNull(message = "Shipment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ShipmentStatus status = ShipmentStatus.PLANNED;

    @Column(name = "VEHICLE_ID")
    private Long vehicleId;

    public Shipment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShipmentNumber() {
        return shipmentNumber;
    }

    public void setShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCargoDescription() {
        return cargoDescription;
    }

    public void setCargoDescription(String cargoDescription) {
        this.cargoDescription = cargoDescription;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
}
