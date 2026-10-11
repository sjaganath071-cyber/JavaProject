
package com.team11.resourse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "VEHICLES")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicle_seq_gen")
    @SequenceGenerator(
        name = "vehicle_seq_gen",
        sequenceName = "VEHICLE_SEQ",
        allocationSize = 1
    )
    @Column(name = "VEHICLE_ID")
    private Long id;

    @Column(name = "VEHICLE_NUMBER", nullable = false, unique = true, length = 30)
    private String vehicleNumber;

    @Column(name = "VEHICLE_TYPE", nullable = false, length = 50)
    private String vehicleType;

    @Column(name = "CAPACITY", nullable = false)
    private Integer capacity;

    @Column(name = "UNIT_ID")
    private Long unitId;

    @Column(name = "LOCATION_CODE", length = 50)
    private String locationCode;

    @Column(name = "CONDITION_STATUS", nullable = false, length = 30)
    private String condition;

    @Column(name = "STATUS", nullable = false, length = 30)
    private String status = "AVAILABLE";

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Vehicle() {
    }

    public Long getId() {
        return id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
