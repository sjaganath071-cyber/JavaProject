package com.team11.defense_logistics.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.team11.defense_logistics.dto.VehicleDto;
import com.team11.defense_logistics.model.Movement;

@Service
public class MovementService {

    private static final List<String> VALID_STATUSES =
            List.of("Scheduled", "In Transit", "Completed", "Cancelled");

    private final RestClient restClient;
    private final VehicleService vehicleService;

    public MovementService(
            RestClient.Builder restClientBuilder,
            VehicleService vehicleService,
            @Value("${services.shipments.base-url:http://localhost:8082/api/shipments}")
            String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.vehicleService = vehicleService;
    }

    public synchronized List<Movement> findAll() {
        try {
            Map<String, Object>[] response = restClient.get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map[].class);
            if (response == null) {
                return List.of();
            }
            return Arrays.stream(response)
                    .filter(Objects::nonNull)
                    .map(this::toFrontendModel)
                    .toList();
        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to retrieve shipment movements from the Jagan backend.",
                    ex);
        }
    }

    public synchronized Optional<Movement> findById(Long id) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            return response == null ? Optional.empty() : Optional.of(toFrontendModel(response));
        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to retrieve movement with ID " + id + " from the backend.",
                    ex);
        }
    }

    public synchronized List<Movement> byVehicle(Long vehicleId) {
        return findAll().stream()
                .filter(movement -> Objects.equals(movement.getVehicleId(), vehicleId))
                .toList();
    }

    public synchronized List<Movement> byStatus(String status) {
        if (status == null || status.isBlank()) {
            return findAll();
        }
        String normalizedStatus = status.trim();
        return findAll().stream()
                .filter(movement -> movement.getStatus() != null
                        && movement.getStatus().equalsIgnoreCase(normalizedStatus))
                .toList();
    }

    public synchronized Movement create(Movement movement) {
        if (movement == null || movement.getVehicleId() == null || movement.getVehicleId() <= 0) {
            throw new IllegalArgumentException("Positive vehicle ID is required.");
        }
        if (movement.getOrigin() == null || movement.getOrigin().isBlank()
                || movement.getDestination() == null || movement.getDestination().isBlank()) {
            throw new IllegalArgumentException("Origin and destination are required.");
        }

        String origin = movement.getOrigin().trim();
        String destination = movement.getDestination().trim();
        if (origin.equalsIgnoreCase(destination)) {
            throw new IllegalArgumentException("Origin and destination must differ.");
        }

        vehicleService.getVehicleById(movement.getVehicleId());

        if (movement.getAllocationId() != null && movement.getAllocationId() <= 0) {
            throw new IllegalArgumentException("Allocation ID must be positive.");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("shipmentNumber", "MOV-" + System.currentTimeMillis());
        payload.put("origin", origin);
        payload.put("destination", destination);
        payload.put("cargoDescription", movement.getNotes() == null || movement.getNotes().isBlank() ? null : movement.getNotes().trim());
        payload.put("status", toShipmentStatus(movement.getStatus() == null ? "Scheduled" : movement.getStatus()));
        payload.put("vehicleId", movement.getVehicleId());

        try {
            Map<String, Object> response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            if (response == null) {
                throw new IllegalStateException("The backend returned no shipment record.");
            }
            return toFrontendModel(response);
        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to create the shipment movement in the backend.",
                    ex);
        }
    }

    public synchronized Movement updateStatus(Long id, String status) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Movement ID is required.");
        }
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid movement status: " + status);
        }

        try {
            Map<String, Object> current = restClient.get()
                    .uri("/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
            if (current == null) {
                throw new IllegalArgumentException("Movement not found: " + id);
            }

            current.put("status", toShipmentStatus(status));
            Map<String, Object> response = restClient.put()
                    .uri("/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(current)
                    .retrieve()
                    .body(Map.class);
            if (response == null) {
                throw new IllegalStateException("The backend returned no updated shipment record.");
            }
            return toFrontendModel(response);
        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to update movement status in the backend.",
                    ex);
        }
    }

    private Movement toFrontendModel(Map<String, Object> response) {
        Movement movement = new Movement();
        movement.setId(asLong(response.get("id")));
        movement.setVehicleId(asLong(response.get("vehicleId")));
        movement.setOrigin(asString(response.get("origin")));
        movement.setDestination(asString(response.get("destination")));
        movement.setNotes(asString(response.get("cargoDescription")));
        movement.setStatus(fromShipmentStatus(asString(response.get("status"))));
        return movement;
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String toShipmentStatus(String status) {
        if (status == null || status.isBlank()) {
            return "PLANNED";
        }
        return switch (status.trim()) {
            case "Scheduled" -> "PLANNED";
            case "In Transit" -> "IN_TRANSIT";
            case "Completed" -> "DELIVERED";
            case "Cancelled" -> "CANCELLED";
            default -> status.trim().toUpperCase(Locale.ROOT);
        };
    }

    private String fromShipmentStatus(String status) {
        if (status == null || status.isBlank()) {
            return "Scheduled";
        }
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "PLANNED" -> "Scheduled";
            case "IN_TRANSIT" -> "In Transit";
            case "DELIVERED" -> "Completed";
            case "CANCELLED" -> "Cancelled";
            default -> "Scheduled";
        };
    }
}
