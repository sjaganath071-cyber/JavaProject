package com.team11.defense_logistics.service;

import com.team11.defense_logistics.dto.InventoryItemDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {

    private final RestClient restClient;

    public InventoryService(
            RestClient.Builder restClientBuilder,
            @Value("${inventory.api.base-url:http://localhost:8081/api/v1/inventory/items}")
            String baseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public List<InventoryItemDto> getAllItems() {
        try {
            List<Map<String, Object>> items = restClient.get()
                    .uri("")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });

            if (items == null || items.isEmpty()) {
                return Collections.emptyList();
            }

            List<InventoryItemDto> mapped = new ArrayList<>();
            for (Map<String, Object> item : items) {
                mapped.add(mapItem(item));
            }
            return mapped;

        } catch (RestClientException exception) {
            throw new InventoryServiceException(
                    "Unable to retrieve inventory. Check whether the inventory backend is running.",
                    exception
            );
        }
    }

    public InventoryItemDto getItemById(Long id) {
        try {
            Map<String, Object> item = restClient.get()
                    .uri("/{id}", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (item == null) {
                throw new InventoryServiceException("Inventory item was not found.");
            }

            return mapItem(item);

        } catch (RestClientException exception) {
            throw new InventoryServiceException(
                    "Unable to retrieve inventory item with ID " + id + ".",
                    exception
            );
        }
    }

    public InventoryItemDto createItem(InventoryItemDto item) {
        try {
            Map<String, Object> request = toInventoryRequest(item);

            Map<String, Object> createdItem = restClient.post()
                    .uri("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (createdItem == null) {
                throw new InventoryServiceException(
                        "The inventory backend returned an empty response."
                );
            }

            return mapItem(createdItem);

        } catch (RestClientException exception) {
            throw new InventoryServiceException(
                    "Unable to create inventory item. Verify the item code and backend API.",
                    exception
            );
        }
    }

    public InventoryItemDto updateItem(Long id, InventoryItemDto item) {
        try {
            Map<String, Object> request = toInventoryRequest(item);

            Map<String, Object> updatedItem = restClient.put()
                    .uri("/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (updatedItem == null) {
                throw new InventoryServiceException(
                        "The inventory backend returned an empty response."
                );
            }

            return mapItem(updatedItem);

        } catch (RestClientException exception) {
            throw new InventoryServiceException(
                    "Unable to update inventory item with ID " + id + ".",
                    exception
            );
        }
    }

    private InventoryItemDto mapItem(Map<String, Object> payload) {
        InventoryItemDto dto = new InventoryItemDto();
        dto.setId(asLong(payload.get("itemId")));
        dto.setItemCode(safeString(payload.get("itemCode")));
        dto.setName(safeString(payload.get("itemName")));
        dto.setCategory(safeString(payload.get("category")));
        dto.setUnit(safeString(payload.get("unitOfMeasure")));
        dto.setQuantity(asInteger(payload.get("quantityOnHand")));
        dto.setMinimumStock(asInteger(payload.get("reorderThreshold")));
        dto.setStorageLocation(safeString(payload.get("location")));
        dto.setDescription("");
        return dto;
    }

    private Map<String, Object> toInventoryRequest(InventoryItemDto item) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("itemCode", item.getItemCode());
        request.put("itemName", item.getName());
        request.put("category", item.getCategory());
        request.put("unitOfMeasure", item.getUnit());
        request.put("unitId", parseLong(item.getUnit()) != null ? parseLong(item.getUnit()) : 1L);
        request.put("location", item.getStorageLocation());
        request.put("quantityOnHand", item.getQuantity() == null ? BigDecimal.ZERO : BigDecimal.valueOf(item.getQuantity()));
        request.put("quantityReserved", BigDecimal.ZERO);
        request.put("reorderThreshold", item.getMinimumStock() == null ? BigDecimal.ZERO : BigDecimal.valueOf(item.getMinimumStock()));
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

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String safeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static class InventoryServiceException extends RuntimeException {

        public InventoryServiceException(String message) {
            super(message);
        }

        public InventoryServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
