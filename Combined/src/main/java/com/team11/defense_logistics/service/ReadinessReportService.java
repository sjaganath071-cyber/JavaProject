package com.team11.defense_logistics.service;

import com.team11.defense_logistics.dto.AssetDto;
import com.team11.defense_logistics.dto.PersonnelDto;
import com.team11.defense_logistics.dto.ReadinessReportDto;
import com.team11.defense_logistics.dto.VehicleDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
public class ReadinessReportService {

    private final RestClient restClient;
    private final String personnelApiUrl;
    private final String assetApiUrl;
    private final String vehicleApiUrl;

    public ReadinessReportService(
            RestClient.Builder restClientBuilder,
            @Value("${services.personnel.base-url:http://localhost:8081/api/v1/personnel}") String personnelApiUrl,
            @Value("${services.asset.base-url:http://localhost:8082/api/v1/assets}") String assetApiUrl,
            @Value("${services.vehicle.base-url:http://localhost:8083/api/v1/vehicles}") String vehicleApiUrl) {

        this.restClient = restClientBuilder.build();
        this.personnelApiUrl = personnelApiUrl;
        this.assetApiUrl = assetApiUrl;
        this.vehicleApiUrl = vehicleApiUrl;
    }

    public ReadinessReportDto getReadinessReport() {
        ReadinessReportDto report = new ReadinessReportDto();
        report.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));

        List<PersonnelDto> personnel = retrievePersonnel();
        if (personnel == null || personnel.isEmpty()) {
            report.setPersonnelDataAvailable(false);
            report.setMessage("Personnel readiness data is unavailable because the connected backend service did not return records.");
        } else {
            int totalPersonnel = personnel.size();
            int availablePersonnel = 0;

            for (PersonnelDto member : personnel) {
                if (isPersonnelAvailable(member)) {
                    availablePersonnel++;
                }
            }

            int unavailablePersonnel = totalPersonnel - availablePersonnel;
            double personnelReadiness = calculatePercentage(totalPersonnel, availablePersonnel);

            report.setTotalPersonnel(totalPersonnel);
            report.setAvailablePersonnel(availablePersonnel);
            report.setUnavailablePersonnel(unavailablePersonnel);
            report.setPersonnelReadinessPercentage(personnelReadiness);
            report.setPersonnelDataAvailable(true);
        }

        List<AssetDto> assets = retrieveAssets();
        if (assets == null || assets.isEmpty()) {
            report.setEquipmentDataAvailable(false);
            if (report.getMessage() == null || report.getMessage().isBlank()) {
                report.setMessage("Equipment readiness data is unavailable because the connected backend service did not return records.");
            }
        } else {
            int totalEquipment = assets.size();
            int serviceableEquipment = 0;

            for (AssetDto asset : assets) {
                if (isServiceableAsset(asset)) {
                    serviceableEquipment++;
                }
            }

            int unserviceableEquipment = totalEquipment - serviceableEquipment;
            double equipmentReadiness = calculatePercentage(totalEquipment, serviceableEquipment);

            report.setTotalEquipment(totalEquipment);
            report.setServiceableEquipment(serviceableEquipment);
            report.setUnserviceableEquipment(unserviceableEquipment);
            report.setEquipmentReadinessPercentage(equipmentReadiness);
            report.setEquipmentDataAvailable(true);
        }

        List<VehicleDto> vehicles = retrieveVehicles();
        if (vehicles == null || vehicles.isEmpty()) {
            report.setVehicleDataAvailable(false);
            if (report.getMessage() == null || report.getMessage().isBlank()) {
                report.setMessage("Vehicle readiness data is unavailable because the connected backend service did not return records.");
            }
        } else {
            int totalVehicles = vehicles.size();
            int availableVehicles = 0;

            for (VehicleDto vehicle : vehicles) {
                if (isVehicleAvailable(vehicle)) {
                    availableVehicles++;
                }
            }

            int unavailableVehicles = totalVehicles - availableVehicles;
            double vehicleReadiness = calculatePercentage(totalVehicles, availableVehicles);

            report.setTotalVehicles(totalVehicles);
            report.setAvailableVehicles(availableVehicles);
            report.setUnavailableVehicles(unavailableVehicles);
            report.setVehicleReadinessPercentage(vehicleReadiness);
            report.setVehicleDataAvailable(true);
        }

        int validCategories = 0;
        double readinessTotal = 0.0;

        if (report.isPersonnelDataAvailable()) {
            validCategories++;
            readinessTotal += report.getPersonnelReadinessPercentage();
        }

        if (report.isEquipmentDataAvailable()) {
            validCategories++;
            readinessTotal += report.getEquipmentReadinessPercentage();
        }

        if (report.isVehicleDataAvailable()) {
            validCategories++;
            readinessTotal += report.getVehicleReadinessPercentage();
        }

        if (validCategories > 0) {
            double overallReadiness = readinessTotal / validCategories;
            report.setOverallReadinessPercentage(roundPercentage(overallReadiness));
            report.setTotalResources(report.getAvailablePersonnel() + report.getServiceableEquipment() + report.getAvailableVehicles());
            report.setAvailableResources(report.getAvailablePersonnel() + report.getServiceableEquipment() + report.getAvailableVehicles());
            report.setUnavailableResources(report.getUnavailablePersonnel() + report.getUnserviceableEquipment() + report.getUnavailableVehicles());

            String categoryCoverage = buildCoverageText(report);
            report.setMessage(categoryCoverage + " Overall readiness is calculated from the available categories only.");
        } else {
            report.setOverallReadinessPercentage(0.0);
            report.setTotalResources(0);
            report.setAvailableResources(0);
            report.setUnavailableResources(0);
            if (report.getMessage() == null || report.getMessage().isBlank()) {
                report.setMessage("No category has valid readiness data. Backend service data is unavailable or no usable records were returned.");
            }
        }

        return report;
    }

    private String buildCoverageText(ReadinessReportDto report) {
        List<String> covered = new ArrayList<>();

        if (report.isPersonnelDataAvailable()) {
            covered.add("personnel");
        }
        if (report.isEquipmentDataAvailable()) {
            covered.add("equipment");
        }
        if (report.isVehicleDataAvailable()) {
            covered.add("vehicles");
        }

        if (covered.isEmpty()) {
            return "No data coverage";
        }
        if (covered.size() == 1) {
            return "Coverage includes " + covered.get(0) + ".";
        }
        return "Coverage includes " + String.join(", ", covered.subList(0, covered.size() - 1)) + " and " + covered.get(covered.size() - 1) + ".";
    }

    private List<PersonnelDto> retrievePersonnel() {
        try {
            List<PersonnelDto> personnel = restClient.get()
                    .uri(personnelApiUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PersonnelDto>>() {});
            return personnel == null ? Collections.emptyList() : personnel;
        } catch (RestClientException exception) {
            return Collections.emptyList();
        }
    }

    private List<AssetDto> retrieveAssets() {
        try {
            List<AssetDto> assets = restClient.get()
                    .uri(assetApiUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<AssetDto>>() {});
            return assets == null ? Collections.emptyList() : assets;
        } catch (RestClientException exception) {
            return Collections.emptyList();
        }
    }

    private List<VehicleDto> retrieveVehicles() {
        try {
            List<VehicleDto> vehicles = restClient.get()
                    .uri(vehicleApiUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<VehicleDto>>() {});
            return vehicles == null ? Collections.emptyList() : vehicles;
        } catch (RestClientException exception) {
            return Collections.emptyList();
        }
    }

    private boolean isPersonnelAvailable(PersonnelDto personnel) {
        if (personnel == null || personnel.getAvailability() == null) {
            return false;
        }

        String availability = personnel.getAvailability().trim().toUpperCase(Locale.ROOT);
        return "AVAILABLE".equals(availability)
                || "READY".equals(availability)
                || "FULLY_AVAILABLE".equals(availability)
                || "ON_STATION".equals(availability);
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

        boolean knownServiceable = combined.contains("SERVICEABLE")
                || combined.contains("OPERATIONAL")
                || combined.contains("READY")
                || combined.contains("WORKING")
                || combined.contains("FUNCTIONAL")
                || combined.contains("AVAILABLE");

        boolean knownUnserviceable = combined.contains("UNSERVICEABLE")
                || combined.contains("NOT_SERVICEABLE")
                || combined.contains("OUT_OF_SERVICE")
                || combined.contains("DAMAGED")
                || combined.contains("DEFECTIVE");

        return knownServiceable && !knownUnserviceable;
    }

    private boolean isVehicleAvailable(VehicleDto vehicle) {
        if (vehicle == null) {
            return false;
        }

        String combined = ((vehicle.getCondition() == null ? "" : vehicle.getCondition()) + " "
                + (vehicle.getStatus() == null ? "" : vehicle.getStatus())).trim().toUpperCase(Locale.ROOT);

        if (combined.isBlank()) {
            return false;
        }

        boolean knownAvailable = combined.contains("AVAILABLE")
                || combined.contains("READY")
                || combined.contains("OPERATIONAL")
                || combined.contains("SERVICEABLE")
                || combined.contains("FUNCTIONAL");

        boolean knownUnavailable = combined.contains("UNAVAILABLE")
                || combined.contains("OUT_OF_SERVICE")
                || combined.contains("MAINTENANCE")
                || combined.contains("DAMAGED")
                || combined.contains("BROKEN");

        return knownAvailable && !knownUnavailable;
    }

    private double calculatePercentage(int total, int available) {
        if (total <= 0) {
            return 0.0;
        }
        return roundPercentage((available * 100.0) / total);
    }

    private double roundPercentage(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }
}
