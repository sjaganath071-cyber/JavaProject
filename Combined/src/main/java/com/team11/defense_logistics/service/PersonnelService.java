package com.team11.defense_logistics.service;

import com.team11.defense_logistics.dto.PersonnelDto;
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
public class PersonnelService {

    private final RestClient restClient;
    private final String personnelApiUrl;

    public PersonnelService(
            RestClient.Builder restClientBuilder,
            @Value("${services.personnel.base-url:http://localhost:8081/api/v1/personnel}")
            String personnelApiUrl) {

        this.restClient = restClientBuilder.build();
        this.personnelApiUrl = personnelApiUrl;
    }

    public List<PersonnelDto> getAllPersonnel() {
        try {
            List<Map<String, Object>> personnel = restClient.get()
                    .uri(personnelApiUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

            if (personnel == null || personnel.isEmpty()) {
                return new ArrayList<>();
            }

            List<PersonnelDto> mapped = new ArrayList<>();
            for (Map<String, Object> record : personnel) {
                mapped.add(mapPersonnel(record));
            }
            return mapped;

        } catch (RestClientException exception) {
            throw new PersonnelServiceException(
                    "Unable to retrieve personnel from the backend service.",
                    exception
            );
        }
    }

    public PersonnelDto getPersonnelById(Long id) {
        try {
            Map<String, Object> personnel = restClient.get()
                    .uri(personnelApiUrl + "/{id}", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (personnel == null) {
                throw new PersonnelServiceException(
                        "The backend returned no personnel record."
                );
            }

            return mapPersonnel(personnel);

        } catch (RestClientException exception) {
            throw new PersonnelServiceException(
                    "Unable to retrieve personnel record " + id + ".",
                    exception
            );
        }
    }

    public PersonnelDto createPersonnel(PersonnelDto personnel) {
        try {
            Map<String, Object> request = toPersonnelCreateRequest(personnel);

            Map<String, Object> created = restClient.post()
                    .uri(personnelApiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (created == null) {
                throw new PersonnelServiceException(
                        "The backend returned no created personnel record."
                );
            }

            return mapPersonnel(created);

        } catch (RestClientException exception) {
            throw new PersonnelServiceException(
                    "Unable to create personnel. Check the submitted information.",
                    exception
            );
        }
    }

    public PersonnelDto updatePersonnel(Long id, PersonnelDto personnel) {
        try {
            Map<String, Object> request = toPersonnelUpdateRequest(personnel);

            Map<String, Object> updated = restClient.put()
                    .uri(personnelApiUrl + "/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (updated == null) {
                throw new PersonnelServiceException(
                        "The backend returned no updated personnel record."
                );
            }

            return mapPersonnel(updated);

        } catch (RestClientException exception) {
            throw new PersonnelServiceException(
                    "Unable to update personnel record " + id + ".",
                    exception
            );
        }
    }

    public PersonnelDto updateAvailability(Long id, String availability) {
        try {
            Map<String, Object> updated = restClient.patch()
                    .uri(personnelApiUrl + "/{id}/availability", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("availability", availability))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (updated == null) {
                throw new PersonnelServiceException(
                        "The backend returned no updated availability record."
                );
            }

            return mapPersonnel(updated);

        } catch (RestClientException exception) {
            throw new PersonnelServiceException(
                    "Unable to update personnel availability.",
                    exception
            );
        }
    }

    private PersonnelDto mapPersonnel(Map<String, Object> payload) {
        PersonnelDto dto = new PersonnelDto();
        dto.setId(asLong(payload.get("personnelId")));
        dto.setPersonnelCode(safeString(payload.get("personnelCode")));
        dto.setName(safeString(payload.get("fullName")));
        dto.setRole(safeString(payload.get("role")));
        dto.setUnit(safeString(payload.get("unitId")));
        dto.setPosting(safeString(payload.get("postingLocation")));
        dto.setAvailability(safeString(payload.get("availability")));
        dto.setActive(Boolean.TRUE.equals(payload.get("active")));
        return dto;
    }

    private Map<String, Object> toPersonnelCreateRequest(PersonnelDto personnel) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("personnelCode", personnel.getPersonnelCode());
        request.put("fullName", personnel.getName());
        request.put("role", personnel.getRole());
        request.put("unitId", parseLong(personnel.getUnit()));
        request.put("postingLocation", personnel.getPosting());
        request.put("availability", personnel.getAvailability());
        request.put("active", personnel.isActive());
        return request;
    }

    private Map<String, Object> toPersonnelUpdateRequest(PersonnelDto personnel) {
        return toPersonnelCreateRequest(personnel);
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

    public static class PersonnelServiceException extends RuntimeException {

        public PersonnelServiceException(String message) {
            super(message);
        }

        public PersonnelServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
