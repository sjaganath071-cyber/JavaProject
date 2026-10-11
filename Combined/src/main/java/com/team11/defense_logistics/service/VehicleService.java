package com.team11.defense_logistics.service;

import com.team11.defense_logistics.dto.VehicleDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VehicleService {

    private final RestClient restClient;
    private final String vehicleApiUrl;

    public VehicleService(
            RestClient.Builder restClientBuilder,
            @Value("${services.vehicle.base-url:http://localhost:8081/api/v1/vehicles}")
            String vehicleApiUrl) {

        this.restClient = restClientBuilder.build();
        this.vehicleApiUrl = vehicleApiUrl;
    }

    public List<VehicleDto> getAllVehicles() {
        try {
            List<Map<String, Object>> vehicles = restClient.get()
                    .uri(vehicleApiUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (vehicles == null || vehicles.isEmpty()) {
                return new ArrayList<>();
            }

            List<VehicleDto> mapped = new ArrayList<>();
            for (Map<String, Object> vehicle : vehicles) {
                mapped.add(mapVehicle(vehicle));
            }
            return mapped;

        } catch (RestClientException exception) {
            throw new VehicleServiceException(
                    "Unable to retrieve vehicles from the backend service.",
                    exception
            );
        }
    }

    public VehicleDto getVehicleById(Long id) {
        try {
            Map<String, Object> vehicle = restClient.get()
                    .uri(vehicleApiUrl + "/{id}", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (vehicle == null) {
                throw new VehicleServiceException(
                        "The backend returned no vehicle record."
                );
            }

            return mapVehicle(vehicle);

        } catch (RestClientException exception) {
            throw new VehicleServiceException(
                    "Unable to retrieve vehicle record " + id + ".",
                    exception
            );
        }
    }

    public VehicleDto createVehicle(VehicleDto vehicle) {
        try {
            Map<String, Object> request = toVehicleRequest(vehicle);

            Map<String, Object> created = restClient.post()
                    .uri(vehicleApiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (created == null) {
                throw new VehicleServiceException(
                        "The backend returned no created vehicle record."
                );
            }

            return mapVehicle(created);

        } catch (RestClientException exception) {
            throw new VehicleServiceException(
                    "Unable to register the vehicle.",
                    exception
            );
        }
    }

    public VehicleDto updateVehicle(Long id, VehicleDto vehicle) {
        try {
            Map<String, Object> request = toVehicleRequest(vehicle);

            Map<String, Object> updated = restClient.put()
                    .uri(vehicleApiUrl + "/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (updated == null) {
                throw new VehicleServiceException(
                        "The backend returned no updated vehicle record."
                );
            }

            return mapVehicle(updated);

        } catch (RestClientException exception) {
            throw new VehicleServiceException(
                    "Unable to update vehicle record " + id + ".",
                    exception
            );
        }
    }

    private VehicleDto mapVehicle(Map<String, Object> payload) {
        VehicleDto dto = new VehicleDto();
        dto.setId(asLong(payload.get("vehicleId")));
        dto.setVehicleCode(safeString(payload.get("vehicleNumber")));
        dto.setName(safeString(payload.get("vehicleType")));
        dto.setType(safeString(payload.get("vehicleType")));
        dto.setUnit(safeString(payload.get("unitId")));
        dto.setLocation(safeString(payload.get("locationCode")));
        dto.setCondition(safeString(payload.get("condition")));
        dto.setStatus(safeString(payload.get("status")));
        return dto;
    }

    private Map<String, Object> toVehicleRequest(VehicleDto vehicle) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("vehicleNumber", vehicle.getVehicleCode());
        request.put("vehicleType", vehicle.getType() == null ? vehicle.getName() : vehicle.getType());
        request.put("capacity", 1);
        request.put("unitId", parseLong(vehicle.getUnit()));
        request.put("locationCode", vehicle.getLocation());
        request.put("condition", vehicle.getCondition());
        request.put("status", vehicle.getStatus());
        return request;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static class VehicleServiceException extends RuntimeException {

        public VehicleServiceException(String message) {
            super(message);
        }

        public VehicleServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
