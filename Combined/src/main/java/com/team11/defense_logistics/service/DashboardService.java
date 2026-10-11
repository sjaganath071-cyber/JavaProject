package com.team11.defense_logistics.service;

import com.team11.defense_logistics.dto.AssetDto;
import com.team11.defense_logistics.dto.DashboardSummaryDto;
import com.team11.defense_logistics.dto.LogisticsRequestDto;
import com.team11.defense_logistics.dto.MaintenanceDto;
import com.team11.defense_logistics.dto.PersonnelDto;
import com.team11.defense_logistics.dto.VehicleDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PersonnelService personnelService;
    private final AssetService assetService;
    private final VehicleService vehicleService;
    private final MaintenanceService maintenanceService;
    private final LogisticsRequestService logisticsRequestService;

    public DashboardService(
            PersonnelService personnelService,
            AssetService assetService,
            VehicleService vehicleService,
            MaintenanceService maintenanceService,
            LogisticsRequestService logisticsRequestService) {

        this.personnelService = personnelService;
        this.assetService = assetService;
        this.vehicleService = vehicleService;
        this.maintenanceService = maintenanceService;
        this.logisticsRequestService = logisticsRequestService;
    }

    public DashboardSummaryDto getDashboardSummary() {
        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setLastUpdated(LocalDateTime.now().format(FORMATTER));

        List<PersonnelDto> personnel = getPersonnel();
        if (personnel != null) {
            int total = personnel.size();
            int available = 0;
            for (PersonnelDto member : personnel) {
                if (isAvailablePersonnel(member)) {
                    available++;
                }
            }
            summary.setTotalPersonnel(total);
            summary.setAvailablePersonnel(available);
            summary.setPersonnelDataAvailable(true);
        } else {
            summary.setPersonnelDataAvailable(false);
        }

        List<AssetDto> equipment = getAssets();
        if (equipment != null) {
            int total = equipment.size();
            int serviceable = 0;
            for (AssetDto asset : equipment) {
                if (isServiceableAsset(asset)) {
                    serviceable++;
                }
            }
            summary.setTotalEquipment(total);
            summary.setServiceableEquipment(serviceable);
            summary.setEquipmentDataAvailable(true);
            summary.setAvailableAssets((long) serviceable);
        } else {
            summary.setEquipmentDataAvailable(false);
        }

        List<VehicleDto> vehicles = getVehicles();
        if (vehicles != null) {
            int total = vehicles.size();
            int available = 0;
            for (VehicleDto vehicle : vehicles) {
                if (isAvailableVehicle(vehicle)) {
                    available++;
                }
            }
            summary.setTotalVehicles(total);
            summary.setAvailableVehicles(available);
            summary.setVehicleDataAvailable(true);
        } else {
            summary.setVehicleDataAvailable(false);
        }

        List<MaintenanceDto> maintenance = getMaintenance();
        if (maintenance != null) {
            int total = maintenance.size();
            int pending = 0;
            for (MaintenanceDto record : maintenance) {
                if (isPendingMaintenance(record)) {
                    pending++;
                }
            }
            summary.setTotalMaintenanceTasks(total);
            summary.setPendingMaintenanceTasks(pending);
            summary.setMaintenanceDataAvailable(true);
            summary.setMaintenanceDue((long) pending);
        } else {
            summary.setMaintenanceDataAvailable(false);
        }

        List<LogisticsRequestDto> requests = getRequests();
        if (requests != null) {
            int total = requests.size();
            int pending = 0;
            int critical = 0;
            for (LogisticsRequestDto request : requests) {
                if (isPendingRequest(request)) {
                    pending++;
                }
                if (isCriticalRequest(request)) {
                    critical++;
                }
            }
            summary.setTotalLogisticsRequests(total);
            summary.setPendingLogisticsRequests(pending);
            summary.setCriticalLogisticsRequests(critical);
            summary.setLogisticsRequestDataAvailable(true);
            summary.setPendingRequests((long) pending);
        } else {
            summary.setLogisticsRequestDataAvailable(false);
        }

        List<String> unavailableSources = new ArrayList<>();
        if (!Boolean.TRUE.equals(summary.getPersonnelDataAvailable())) {
            unavailableSources.add("Personnel");
        }
        if (!Boolean.TRUE.equals(summary.getEquipmentDataAvailable())) {
            unavailableSources.add("Equipment");
        }
        if (!Boolean.TRUE.equals(summary.getVehicleDataAvailable())) {
            unavailableSources.add("Vehicles");
        }
        if (!Boolean.TRUE.equals(summary.getMaintenanceDataAvailable())) {
            unavailableSources.add("Maintenance");
        }
        if (!Boolean.TRUE.equals(summary.getLogisticsRequestDataAvailable())) {
            unavailableSources.add("Logistics requests");
        }

        if (unavailableSources.isEmpty()) {
            summary.setStatusMessage("All monitored modules are reporting live dashboard data.");
        } else if (unavailableSources.size() == 1) {
            summary.setStatusMessage(unavailableSources.get(0) + " data is currently unavailable.");
        } else {
            summary.setStatusMessage("Some module data is currently unavailable: " + String.join(", ", unavailableSources) + ".");
        }

        return summary;
    }

    public List<Map<String, String>> getRecentRequests() {
        List<Map<String, String>> recent = new ArrayList<>();
        List<LogisticsRequestDto> requests = getRequests();
        if (requests == null || requests.isEmpty()) {
            return recent;
        }

        int maxResults = Math.min(3, requests.size());
        for (int index = 0; index < maxResults; index++) {
            LogisticsRequestDto request = requests.get(index);
            String resource = request.getAssignedResource() != null && !request.getAssignedResource().isBlank()
                    ? request.getAssignedResource()
                    : request.getRequestType();

            recent.add(Map.of(
                    "id", request.getRequestCode() == null || request.getRequestCode().isBlank() ? "REQ-" + request.getId() : request.getRequestCode(),
                    "unit", request.getRequestingUnit() == null ? "Unknown" : request.getRequestingUnit(),
                    "resource", resource == null ? "Resource" : resource,
                    "status", request.getStatus() == null ? "PENDING" : request.getStatus()
            ));
        }
        return recent;
    }

    private List<PersonnelDto> getPersonnel() {
        try {
            List<PersonnelDto> personnel = personnelService.getAllPersonnel();
            return personnel == null ? Collections.emptyList() : personnel;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private List<AssetDto> getAssets() {
        try {
            List<AssetDto> assets = assetService.getAllAssets();
            return assets == null ? Collections.emptyList() : assets;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private List<VehicleDto> getVehicles() {
        try {
            List<VehicleDto> vehicles = vehicleService.getAllVehicles();
            return vehicles == null ? Collections.emptyList() : vehicles;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private List<MaintenanceDto> getMaintenance() {
        try {
            List<MaintenanceDto> records = maintenanceService.getAllRecords();
            return records == null ? Collections.emptyList() : records;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private List<LogisticsRequestDto> getRequests() {
        try {
            List<LogisticsRequestDto> requests = logisticsRequestService.getAllRequests();
            return requests == null ? Collections.emptyList() : requests;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private boolean isAvailablePersonnel(PersonnelDto member) {
        if (member == null || member.getAvailability() == null) {
            return false;
        }

        String availability = member.getAvailability().trim().toUpperCase(Locale.ROOT);
        return "AVAILABLE".equals(availability)
                || "READY".equals(availability)
                || "ON_STATION".equals(availability)
                || "FULLY_AVAILABLE".equals(availability);
    }

    private boolean isServiceableAsset(AssetDto asset) {
        if (asset == null) {
            return false;
        }

        String combined = ((asset.getCondition() == null ? "" : asset.getCondition()) + " "
                + (asset.getStatus() == null ? "" : asset.getStatus())).trim().toUpperCase(Locale.ROOT);
        if (combined.isBlank()) {
            return false;
        }

        boolean serviceable = combined.contains("SERVICEABLE")
                || combined.contains("OPERATIONAL")
                || combined.contains("READY")
                || combined.contains("AVAILABLE")
                || combined.contains("FUNCTIONAL");
        boolean unserviceable = combined.contains("UNSERVICEABLE")
                || combined.contains("OUT_OF_SERVICE")
                || combined.contains("DAMAGED")
                || combined.contains("BROKEN")
                || combined.contains("DEFECTIVE");
        return serviceable && !unserviceable;
    }

    private boolean isAvailableVehicle(VehicleDto vehicle) {
        if (vehicle == null) {
            return false;
        }

        String combined = ((vehicle.getCondition() == null ? "" : vehicle.getCondition()) + " "
                + (vehicle.getStatus() == null ? "" : vehicle.getStatus())).trim().toUpperCase(Locale.ROOT);
        if (combined.isBlank()) {
            return false;
        }

        boolean available = combined.contains("AVAILABLE")
                || combined.contains("READY")
                || combined.contains("OPERATIONAL")
                || combined.contains("SERVICEABLE")
                || combined.contains("FUNCTIONAL");
        boolean unavailable = combined.contains("UNAVAILABLE")
                || combined.contains("OUT_OF_SERVICE")
                || combined.contains("DAMAGED")
                || combined.contains("BROKEN")
                || combined.contains("MAINTENANCE");
        return available && !unavailable;
    }

    private boolean isPendingMaintenance(MaintenanceDto record) {
        if (record == null || record.getStatus() == null) {
            return false;
        }

        String status = record.getStatus().trim().toUpperCase(Locale.ROOT);
        return "PENDING".equals(status)
                || "SCHEDULED".equals(status)
                || "IN_PROGRESS".equals(status)
                || "OVERDUE".equals(status);
    }

    private boolean isPendingRequest(LogisticsRequestDto request) {
        if (request == null || request.getStatus() == null) {
            return false;
        }

        String status = request.getStatus().trim().toUpperCase(Locale.ROOT);
        return "PENDING".equals(status)
                || "NEW".equals(status)
                || "REQUESTED".equals(status)
                || "IN_REVIEW".equals(status);
    }

    private boolean isCriticalRequest(LogisticsRequestDto request) {
        if (request == null || request.getPriority() == null) {
            return false;
        }

        String priority = request.getPriority().trim().toUpperCase(Locale.ROOT);
        return "CRITICAL".equals(priority)
                || "URGENT".equals(priority)
                || "HIGH".equals(priority);
    }
}
