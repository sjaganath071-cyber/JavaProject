package com.team11.defense_logistics.service;

import com.team11.defense_logistics.dto.MaintenanceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaintenanceService {

    private final RestClient restClient;

    public MaintenanceService(
            RestClient.Builder restClientBuilder,
            @Value("${maintenance.api.base-url:http://localhost:8081/api/v1/maintenance}")
            String baseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public List<MaintenanceDto> getAllRecords() {
        try {
            List<Map<String, Object>> records = restClient.get()
                    .uri("")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });

            if (records == null || records.isEmpty()) {
                return Collections.emptyList();
            }

            List<MaintenanceDto> mapped = new ArrayList<>();
            for (Map<String, Object> record : records) {
                mapped.add(mapRecord(record));
            }
            return mapped;

        } catch (RestClientException exception) {
            throw new MaintenanceServiceException(
                    "Unable to retrieve maintenance records. Check the maintenance backend.",
                    exception
            );
        }
    }

    public MaintenanceDto getRecordById(Long id) {
        try {
            Map<String, Object> record = restClient.get()
                    .uri("/{id}", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (record == null) {
                throw new MaintenanceServiceException(
                        "Maintenance record was not found."
                );
            }

            return mapRecord(record);

        } catch (RestClientException exception) {
            throw new MaintenanceServiceException(
                    "Unable to retrieve maintenance record with ID " + id + ".",
                    exception
            );
        }
    }

    public MaintenanceDto createRecord(MaintenanceDto record) {
        try {
            Map<String, Object> request = toMaintenanceRequest(record);

            Map<String, Object> createdRecord = restClient.post()
                    .uri("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (createdRecord == null) {
                throw new MaintenanceServiceException(
                        "The maintenance backend returned an empty response."
                );
            }

            return mapRecord(createdRecord);

        } catch (RestClientException exception) {
            throw new MaintenanceServiceException(
                    "Unable to create maintenance record. Verify the backend API.",
                    exception
            );
        }
    }

    public MaintenanceDto updateRecord(Long id, MaintenanceDto record) {
        try {
            Map<String, Object> request = toMaintenanceRequest(record);

            Map<String, Object> updatedRecord = restClient.put()
                    .uri("/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (updatedRecord == null) {
                throw new MaintenanceServiceException(
                        "The maintenance backend returned an empty response."
                );
            }

            return mapRecord(updatedRecord);

        } catch (RestClientException exception) {
            throw new MaintenanceServiceException(
                    "Unable to update maintenance record with ID " + id + ".",
                    exception
            );
        }
    }

    private MaintenanceDto mapRecord(Map<String, Object> payload) {
        MaintenanceDto dto = new MaintenanceDto();
        Long maintenanceId = asLong(payload.get("maintenanceId"));
        dto.setId(maintenanceId);
        dto.setMaintenanceCode(maintenanceId == null ? "" : "MT-" + maintenanceId);
        dto.setResourceCode(safeString(payload.get("resourceId")));
        dto.setResourceType(safeString(payload.get("resourceType")));
        dto.setMaintenanceType(safeString(payload.get("maintenanceType")));
        dto.setDescription(safeString(payload.get("description")));
        dto.setScheduledDate(parseDate(payload.get("scheduledDate")));
        dto.setCompletionDate(parseDate(payload.get("completionDate")));
        dto.setStatus(safeString(payload.get("status")));
        dto.setEstimatedCost(BigDecimal.ZERO);
        dto.setAssignedTechnician(safeString(payload.get("technician")));
        dto.setNotes(safeString(payload.get("serviceNotes")));
        return dto;
    }

    private Map<String, Object> toMaintenanceRequest(MaintenanceDto record) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("resourceType", record.getResourceType());
        request.put("resourceId", parseResourceId(record.getResourceCode(), record.getResourceType()));
        request.put("maintenanceType", record.getMaintenanceType());
        request.put("description", record.getDescription());
        request.put("scheduledDate", record.getScheduledDate() == null ? null : record.getScheduledDate().atStartOfDay());
        request.put("startDate", null);
        request.put("completionDate", record.getCompletionDate() == null ? null : record.getCompletionDate().atStartOfDay());
        request.put("status", record.getStatus());
        request.put("serviceNotes", record.getNotes());
        request.put("technician", record.getAssignedTechnician());
        return request;
    }

    private Long parseResourceId(String resourceCode, String resourceType) {
        if (resourceCode == null || resourceCode.isBlank()) {
            return 1L;
        }

        try {
            return Long.parseLong(resourceCode.trim());
        } catch (NumberFormatException ignored) {
            return 1L;
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

    private LocalDate parseDate(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }

        String text = String.valueOf(value);
        try {
            return LocalDate.parse(text, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception ignored) {
            try {
                return LocalDate.parse(text);
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static class MaintenanceServiceException extends RuntimeException {

        public MaintenanceServiceException(String message) {
            super(message);
        }

        public MaintenanceServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
