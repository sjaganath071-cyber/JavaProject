package com.team11.defense_logistics.dto;

public class ReadinessReportDto {

    private int totalPersonnel;
    private int availablePersonnel;
    private int unavailablePersonnel;

    private int totalEquipment;
    private int serviceableEquipment;
    private int unserviceableEquipment;

    private int totalVehicles;
    private int availableVehicles;
    private int unavailableVehicles;

    private int totalResources;
    private int availableResources;
    private int unavailableResources;

    private double personnelReadinessPercentage;
    private double equipmentReadinessPercentage;
    private double vehicleReadinessPercentage;
    private double overallReadinessPercentage;

    private boolean personnelDataAvailable;
    private boolean equipmentDataAvailable;
    private boolean vehicleDataAvailable;

    private String generatedAt;
    private String message;

    public ReadinessReportDto() {
    }

    public int getTotalPersonnel() {
        return totalPersonnel;
    }

    public void setTotalPersonnel(int totalPersonnel) {
        this.totalPersonnel = totalPersonnel;
    }

    public int getAvailablePersonnel() {
        return availablePersonnel;
    }

    public void setAvailablePersonnel(int availablePersonnel) {
        this.availablePersonnel = availablePersonnel;
    }

    public int getUnavailablePersonnel() {
        return unavailablePersonnel;
    }

    public void setUnavailablePersonnel(int unavailablePersonnel) {
        this.unavailablePersonnel = unavailablePersonnel;
    }

    public int getTotalEquipment() {
        return totalEquipment;
    }

    public void setTotalEquipment(int totalEquipment) {
        this.totalEquipment = totalEquipment;
    }

    public int getServiceableEquipment() {
        return serviceableEquipment;
    }

    public void setServiceableEquipment(int serviceableEquipment) {
        this.serviceableEquipment = serviceableEquipment;
    }

    public int getUnserviceableEquipment() {
        return unserviceableEquipment;
    }

    public void setUnserviceableEquipment(int unserviceableEquipment) {
        this.unserviceableEquipment = unserviceableEquipment;
    }

    public int getTotalVehicles() {
        return totalVehicles;
    }

    public void setTotalVehicles(int totalVehicles) {
        this.totalVehicles = totalVehicles;
    }

    public int getAvailableVehicles() {
        return availableVehicles;
    }

    public void setAvailableVehicles(int availableVehicles) {
        this.availableVehicles = availableVehicles;
    }

    public int getUnavailableVehicles() {
        return unavailableVehicles;
    }

    public void setUnavailableVehicles(int unavailableVehicles) {
        this.unavailableVehicles = unavailableVehicles;
    }

    public int getTotalResources() {
        return totalResources;
    }

    public void setTotalResources(int totalResources) {
        this.totalResources = totalResources;
    }

    public int getAvailableResources() {
        return availableResources;
    }

    public void setAvailableResources(int availableResources) {
        this.availableResources = availableResources;
    }

    public int getUnavailableResources() {
        return unavailableResources;
    }

    public void setUnavailableResources(int unavailableResources) {
        this.unavailableResources = unavailableResources;
    }

    public double getPersonnelReadinessPercentage() {
        return personnelReadinessPercentage;
    }

    public void setPersonnelReadinessPercentage(double personnelReadinessPercentage) {
        this.personnelReadinessPercentage = personnelReadinessPercentage;
    }

    public double getEquipmentReadinessPercentage() {
        return equipmentReadinessPercentage;
    }

    public void setEquipmentReadinessPercentage(double equipmentReadinessPercentage) {
        this.equipmentReadinessPercentage = equipmentReadinessPercentage;
    }

    public double getVehicleReadinessPercentage() {
        return vehicleReadinessPercentage;
    }

    public void setVehicleReadinessPercentage(double vehicleReadinessPercentage) {
        this.vehicleReadinessPercentage = vehicleReadinessPercentage;
    }

    public double getOverallReadinessPercentage() {
        return overallReadinessPercentage;
    }

    public void setOverallReadinessPercentage(double overallReadinessPercentage) {
        this.overallReadinessPercentage = overallReadinessPercentage;
    }

    public boolean isPersonnelDataAvailable() {
        return personnelDataAvailable;
    }

    public void setPersonnelDataAvailable(boolean personnelDataAvailable) {
        this.personnelDataAvailable = personnelDataAvailable;
    }

    public boolean isEquipmentDataAvailable() {
        return equipmentDataAvailable;
    }

    public void setEquipmentDataAvailable(boolean equipmentDataAvailable) {
        this.equipmentDataAvailable = equipmentDataAvailable;
    }

    public boolean isVehicleDataAvailable() {
        return vehicleDataAvailable;
    }

    public void setVehicleDataAvailable(boolean vehicleDataAvailable) {
        this.vehicleDataAvailable = vehicleDataAvailable;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
