package com.team11.defense_logistics.dto;

public class DashboardSummaryDto {
    private Integer totalPersonnel;
    private Integer availablePersonnel;
    private Boolean personnelDataAvailable;

    private Integer totalEquipment;
    private Integer serviceableEquipment;
    private Boolean equipmentDataAvailable;

    private Integer totalVehicles;
    private Integer availableVehicles;
    private Boolean vehicleDataAvailable;

    private Integer totalMaintenanceTasks;
    private Integer pendingMaintenanceTasks;
    private Boolean maintenanceDataAvailable;

    private Integer totalLogisticsRequests;
    private Integer pendingLogisticsRequests;
    private Integer criticalLogisticsRequests;
    private Boolean logisticsRequestDataAvailable;

    private String lastUpdated;
    private String statusMessage;

    private Long availableAssets;
    private Long pendingRequests;
    private Long maintenanceDue;
    private Long lowStockItems;

    public DashboardSummaryDto() {
    }

    public Integer getTotalPersonnel() {
        return totalPersonnel;
    }

    public void setTotalPersonnel(Integer totalPersonnel) {
        this.totalPersonnel = totalPersonnel;
    }

    public Integer getAvailablePersonnel() {
        return availablePersonnel;
    }

    public void setAvailablePersonnel(Integer availablePersonnel) {
        this.availablePersonnel = availablePersonnel;
    }

    public Boolean getPersonnelDataAvailable() {
        return personnelDataAvailable;
    }

    public void setPersonnelDataAvailable(Boolean personnelDataAvailable) {
        this.personnelDataAvailable = personnelDataAvailable;
    }

    public Integer getTotalEquipment() {
        return totalEquipment;
    }

    public void setTotalEquipment(Integer totalEquipment) {
        this.totalEquipment = totalEquipment;
    }

    public Integer getServiceableEquipment() {
        return serviceableEquipment;
    }

    public void setServiceableEquipment(Integer serviceableEquipment) {
        this.serviceableEquipment = serviceableEquipment;
    }

    public Boolean getEquipmentDataAvailable() {
        return equipmentDataAvailable;
    }

    public void setEquipmentDataAvailable(Boolean equipmentDataAvailable) {
        this.equipmentDataAvailable = equipmentDataAvailable;
    }

    public Integer getTotalVehicles() {
        return totalVehicles;
    }

    public void setTotalVehicles(Integer totalVehicles) {
        this.totalVehicles = totalVehicles;
    }

    public Integer getAvailableVehicles() {
        return availableVehicles;
    }

    public void setAvailableVehicles(Integer availableVehicles) {
        this.availableVehicles = availableVehicles;
    }

    public Boolean getVehicleDataAvailable() {
        return vehicleDataAvailable;
    }

    public void setVehicleDataAvailable(Boolean vehicleDataAvailable) {
        this.vehicleDataAvailable = vehicleDataAvailable;
    }

    public Integer getTotalMaintenanceTasks() {
        return totalMaintenanceTasks;
    }

    public void setTotalMaintenanceTasks(Integer totalMaintenanceTasks) {
        this.totalMaintenanceTasks = totalMaintenanceTasks;
    }

    public Integer getPendingMaintenanceTasks() {
        return pendingMaintenanceTasks;
    }

    public void setPendingMaintenanceTasks(Integer pendingMaintenanceTasks) {
        this.pendingMaintenanceTasks = pendingMaintenanceTasks;
    }

    public Boolean getMaintenanceDataAvailable() {
        return maintenanceDataAvailable;
    }

    public void setMaintenanceDataAvailable(Boolean maintenanceDataAvailable) {
        this.maintenanceDataAvailable = maintenanceDataAvailable;
    }

    public Integer getTotalLogisticsRequests() {
        return totalLogisticsRequests;
    }

    public void setTotalLogisticsRequests(Integer totalLogisticsRequests) {
        this.totalLogisticsRequests = totalLogisticsRequests;
    }

    public Integer getPendingLogisticsRequests() {
        return pendingLogisticsRequests;
    }

    public void setPendingLogisticsRequests(Integer pendingLogisticsRequests) {
        this.pendingLogisticsRequests = pendingLogisticsRequests;
    }

    public Integer getCriticalLogisticsRequests() {
        return criticalLogisticsRequests;
    }

    public void setCriticalLogisticsRequests(Integer criticalLogisticsRequests) {
        this.criticalLogisticsRequests = criticalLogisticsRequests;
    }

    public Boolean getLogisticsRequestDataAvailable() {
        return logisticsRequestDataAvailable;
    }

    public void setLogisticsRequestDataAvailable(Boolean logisticsRequestDataAvailable) {
        this.logisticsRequestDataAvailable = logisticsRequestDataAvailable;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Long getAvailableAssets() {
        return availableAssets;
    }

    public void setAvailableAssets(Long availableAssets) {
        this.availableAssets = availableAssets;
    }

    public Long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(Long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public Long getMaintenanceDue() {
        return maintenanceDue;
    }

    public void setMaintenanceDue(Long maintenanceDue) {
        this.maintenanceDue = maintenanceDue;
    }

    public Long getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(Long lowStockItems) {
        this.lowStockItems = lowStockItems;
    }
}

