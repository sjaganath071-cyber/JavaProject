package com.team11.logistics.dto;

public class ShipmentResponse {

    private Long id;
    private String shipmentNumber;
    private String origin;
    private String destination;
    private String cargoDescription;
    private String status;
    private Long vehicleId;

    public ShipmentResponse() {
    }

    public ShipmentResponse(Long id, String shipmentNumber,
            String origin, String destination,
            String cargoDescription, String status, Long vehicleId) {
        this.id = id;
        this.shipmentNumber = shipmentNumber;
        this.origin = origin;
        this.destination = destination;
        this.cargoDescription = cargoDescription;
        this.status = status;
        this.vehicleId = vehicleId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }
}
